/*
 * Copyright 2013-2023, Seqera Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nextflow.executor

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.processor.ArrayTaskPollingMonitor
import nextflow.processor.TaskHandler
import nextflow.processor.TaskMonitor
import nextflow.processor.TaskProcessor
import nextflow.processor.TaskRun
import nextflow.trace.TraceObserver
import nextflow.util.Duration
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
/**
 * Executor that submits tasks in batches to a target executor
 * that supports array jobs.
 *
 * @author Ben Sherman <bentshermann@gmail.com>
 */
@Slf4j
@CompileStatic
class ArrayExecutor extends Executor implements TraceObserver {

    private ArrayTaskAware target

    private Integer arraySize

    private Map<String,List<TaskRun>> queues = new ConcurrentHashMap<>()

    /**
     * Initialize the executor class
     */
    @Override
    protected void register() {
        super.register()

        session.registerObserver(this)

        final targetName = session.getExecConfigProp('array', 'target', 'local') as String
        try {
            target = (ArrayTaskAware)session.executorFactory.getExecutor(targetName, session)
        }
        catch( GroovyCastException e ) {
            throw new IllegalArgumentException("Executor '${targetName}' does not support array jobs")
        }

        arraySize = session.getExecConfigProp('array', 'arraySize', 100) as Integer

        log.debug "Creating 'array' executor > target executor: '${targetName}', array size: ${arraySize}"
    }

    @Override
    TaskMonitor createTaskMonitor() {
        return ArrayTaskPollingMonitor.create(session, name, 100, Duration.of('5 sec'))
    }

    /**
     * Add submitted tasks to the queue, and schedule an array job when
     * the queue reaches the desired size.
     *
     * @param task
     */
    @Override
    synchronized void submit( TaskRun task ) {
        log.trace "Scheduling process: ${task}"

        if( session.isTerminated() )
            new IllegalStateException("Session terminated - Cannot add process to execution array: ${task}")

        // add task to the corresponding process queue
        final process = task.processor.name
        if( process !in queues )
            queues[process] = []

        final array = queues[process]
        array << task

        // schedule array job when the process queue is full
        if( array.size() == arraySize ) {
            log.debug "[ARRAY] Submitting array job for process '${process}'"

            monitor.schedule(target.createArrayTaskHandler(array))
            queues[process] = []
        }
    }

    @Override
    TaskHandler createTaskHandler(TaskRun task) {
        throw new UnsupportedOperationException()
    }

    /**
     * Submit any remaining tasks as a partial batch when a process is closed.
     *
     * @param process
     */
    @Override
    void onProcessClose(String process) {
        final array = queues[process]

        if( array != null && array.size() > 0 ) {
            log.debug "[ARRAY] Submitting remainder array job for process '${process}'"

            monitor.schedule(target.createArrayTaskHandler(array))
        }
    }
}
