package io.github.bootmethod.commandline.impl

import groovy.transform.CompileStatic
import groovyjarjarpicocli.CommandLine
import io.github.bootmethod.commandline.CommandCenter
import io.github.bootmethod.commandline.Command
import io.github.bootmethod.commandline.ResultException
import io.github.bootmethod.commandline.util.ExceptionResult
import io.github.bootmethod.commandline.util.TableDataBuilder
import io.github.bootmethod.componentsbuilder.Components
import io.github.bootmethod.componentsbuilder.annotation.Component
import io.github.bootmethod.componentsbuilder.annotation.Initializer
import org.jline.reader.Completer

import java.util.concurrent.Callable
import java.util.function.BiFunction
import java.util.function.Consumer

/**
 * 
 * 
 * @created : 11/10/2022
 * */
@CompileStatic
class CommandCenterImpl implements io.github.bootmethod.commandline.CommandCenter {
    static class CommandTree {
        Map<String, CommandTree> subTreeMap = new HashMap<>()
        CommandInfo commandInfo
        String[] path

        CommandTree findCommandTree(int offset, String[] path) {
            if (offset <= path.length - 1) {
                CommandTree subTree = subTreeMap.get(path[offset])
                if (subTree) {
                    return subTree.findCommandTree(offset + 1, path)
                }
            }

            return this
        }

        void add(CommandInfo commandInfo) {
            this.add(0, commandInfo)
        }

        Set<String> keySet() {
            return flatMap([:]).keySet()
        }

        Map<String, CommandInfo> flatMap(Map<String, CommandInfo> map) {
            this.eachCommandInfo({ CommandInfo commandInfo ->
                map.put(commandInfo.path.join("/"), commandInfo)
            })

            return map
        }

        void eachCommandInfo(Consumer<CommandInfo> consumer) {
            if (commandInfo) {
                consumer.accept(commandInfo)
            }
            subTreeMap.each {
                it.value.eachCommandInfo(consumer)
            }
        }

        void add(int offset, CommandInfo commandInfo) {
            String name = commandInfo.path[offset]
            CommandTree subTree = subTreeMap.get(name)
            if (subTree == null) {
                String[] treePath = new String[offset + 1]
                System.arraycopy(commandInfo.path, 0, treePath, 0, treePath.length)
                subTree = new CommandTree(path: treePath)
                subTreeMap.put(name, subTree)
            }
            if (offset < commandInfo.path.length - 1) {
                subTree.add(offset + 1, commandInfo)
            } else {
                if (subTree.commandInfo) {
                    throw new RuntimeException("duplicated command:${commandInfo.path.join("/")}")
                }
                subTree.commandInfo = commandInfo
            }
        }
    }

    static class CommandInfo {
        String[] path
        Class commandClass

        Object newCommandObject() {
            return commandClass.getConstructor().newInstance()
        }

        String getName() {
            return path[path.length - 1]
        }
    }

    CommandTree commandTree = new CommandTree(path: [])

    io.github.bootmethod.commandline.CommandCenter addCommand(Class commandClass) {
        def commandAnnotation = commandClass.getAnnotation(CommandLine.Command.class) as CommandLine.Command
        if (commandAnnotation == null) {
            throw new RuntimeException("not a command spec:${commandClass.getName()}")
        }
        String fullName = commandAnnotation.name()
        if (fullName == null) {
            throw new RuntimeException("command name not specified for type:${commandClass.getName()}")
        }
        String[] path = fullName.split("/")
        String name = path[path.length - 1]
        CommandInfo commandInfo = new CommandInfo(path: path, commandClass: commandClass)

        this.commandTree.add(commandInfo)

        return this
    }

    static class ParameterExceptionResult extends ExceptionResult {
        CommandLine.ParameterException exception

        ParameterExceptionResult(CommandLine.ParameterException exception) {
            super(exception)
            this.exception = exception
        }

        @Override
        void print(PrintStream output) {
            super.print(output)
            exception.getCommandLine().usage(output)
        }

    }

    @Component
    Components space

    CommandsCompleter completer = new CommandsCompleter()

    BiFunction<String[], String[], Object> missingCommandHandler = { String[] path, String[] args ->

        throw new ResultException(TableDataBuilder.createTableData("" as Object, commandTree.keySet() as Collection<Object>, "no such command: ${path.join('/')},\nall commands listed below."))
    }


    @Initializer
    void init() {
    }

    @Override
    Completer completer() {
        return this.completer
    }

    @Override
    void close() {

    }

    @Override
    CommandCenter add(Class... commandClass) {

        commandClass.each {
            this.addCommand(it)
        }
        return this
    }

    @Override
    Object execute(String line) throws Exception {
        line = line.trim()
        String[] lineInArray = line.split("\\s+")


        CommandTree commandTree = this.commandTree.findCommandTree(0, lineInArray)
        int offset = commandTree.path.length
        String[] path = new String[offset]
        String[] args = new String[lineInArray.length - offset]
        System.arraycopy(lineInArray, 0, path, 0, path.length)
        System.arraycopy(lineInArray, offset, args, 0, args.length)
        CommandInfo commandInfo = commandTree.commandInfo
        if (commandInfo == null) {
            return this.missingCommandHandler.apply(path, args)
        } else {
            return invoke(commandInfo, args)
        }
    }

    void bindingContext(Object commandObject) {

        commandObject.getClass().getDeclaredFields().each {
            Component componentAno = it.getAnnotation(Component.class)
            if (componentAno == null) {
                return
            }

            Object componentObj = space.getComponent(it.getType(), false)
            if (!componentObj) {
                throw new RuntimeException("cannot binding component for command object:${commandObject}, no component found with type:${it.getType()} ")
            }
            it.setAccessible(true)
            it.set(commandObject, componentObj)

        }

    }

    @Override
    void setMissingCommandHandler(BiFunction<String[], String[], Object> handler) {
        this.missingCommandHandler = handler
    }

    Object invoke(CommandInfo commandInfo, Object[] args) throws Exception {

        CommandLine.ParseResult parseResult
        Object commandObject = commandInfo.newCommandObject()
        bindingContext(commandObject)

        try {
            CommandLine commandLine = new CommandLine(commandObject)
            parseResult = commandLine.parseArgs(args as String[])
        } catch (CommandLine.ParameterException e) {
            return new ParameterExceptionResult(e)
        }

        assert parseResult.commandSpec().commandLine().command == commandObject
        Object result
        if (commandObject instanceof Command) {
            result = ((Command) commandObject).execute()
        } else if (commandObject instanceof Callable) {
            result = ((Callable) commandObject).call()
        } else {
            throw new RuntimeException("not supported command:${commandObject}")
        }

        return result


    }


}
