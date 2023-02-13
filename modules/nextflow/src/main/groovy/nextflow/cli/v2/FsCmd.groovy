/*
 * Copyright 2023, Seqera Labs
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

package nextflow.cli.v2

import groovy.transform.CompileStatic
import nextflow.cli.FsImpl
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters

/**
 * CLI `fs` sub-command (v2)
 *
 * @author Ben Sherman <bentshermann@gmail.com>
 */
@CompileStatic
@Command(
    name = 'fs',
    description = 'Perform basic filesystem operations'
)
class FsCmd extends AbstractCmd {

    @Command(description = 'Copy a file')
    void copy(
            @Parameters(paramLabel = '<source>') String source,
            @Parameters(paramLabel = '<target>') String target) {
        new FsImpl().run(FsImpl.Command.COPY, [ source, target ])
    }

    @Command(description = 'Move a file')
    void move(
            @Parameters(paramLabel = '<source>') String source,
            @Parameters(paramLabel = '<target>') String target) {
        new FsImpl().run(FsImpl.Command.MOVE, [ source, target ])
    }

    @Command(description = 'List the contents of a folder')
    void list(
            @Parameters(paramLabel = '<source>') String source) {
        new FsImpl().run(FsImpl.Command.LIST, [ source ])
    }

    @Command(description = 'Print a file to stdout')
    void cat(
            @Parameters(paramLabel = '<source>') String source) {
        new FsImpl().run(FsImpl.Command.CAT, [ source ])
    }

    @Command(description = 'Remove a file')
    void remove(
            @Parameters(paramLabel = '<source>') String source) {
        new FsImpl().run(FsImpl.Command.REMOVE, [ source ])
    }

}
