/*
 * Copyright 2020-2022, Seqera Labs
 * Copyright 2013-2019, Centre for Genomic Regulation (CRG)
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

package nextflow.cli

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import nextflow.exception.AbortOperationException
import nextflow.plugin.Plugins
import nextflow.scm.AssetManager

/**
 * CLI `drop` sub-command
 *
 * @author Paolo Di Tommaso <paolo.ditommaso@gmail.com>
 */
@Slf4j
@CompileStatic
class DropImpl {

    interface Options {
        String getPipeline()
        boolean getForce()
    }

    @Delegate
    private Options options

    DropImpl(Options options) {
        this.options = options
    }

    void run() {
        Plugins.init()
        def manager = new AssetManager(pipeline)
        if( !manager.localPath.exists() ) {
            throw new AbortOperationException("No match found for: ${pipeline}")
        }

        if( this.force || manager.isClean() ) {
            manager.close()
            if( !manager.localPath.deleteDir() )
                throw new AbortOperationException("Unable to delete project `${manager.project}` -- Check access permissions for path: ${manager.localPath}")
            return
        }

        throw new AbortOperationException("Local project repository contains uncommitted changes -- wont drop it")
    }
}
