package me.vovak.astminer.examples

import astminer.common.model.Node
import astminer.parse.java.GumTreeJavaParser
import java.io.File

const val INPUT_PATH = "input.java"

data class ParameterDefinition(val type: String, val name: String) {
    override fun toString() = "$type $name"
}

data class ReturnStatements(val statements: List<String>) {
    override fun toString(): String = statements.joinToString(" | ")
}

data class MethodMetadata(
        val comment: String,
        val identifier: String,
        val parameters: List<ParameterDefinition>,
        val returnStatements: ReturnStatements
)

fun Node.findChildrenOfType(type: String): List<Node> {
    if (this.getChildren().isEmpty()) return emptyList()
    return this.getChildrenOfType(type) + this.getChildren().map { it.findChildrenOfType(type) }.flatten()
}

fun Node.getName(): String {
    return this.getChildOfType("SimpleName")!!.getToken()
}

fun Node.getParameters(): List<ParameterDefinition> {
    return this.getChildrenOfType("SingleVariableDeclaration").map {
        val type = it.getChildOfType("SimpleType")?.getToken() ?: it.getChildOfType("ParameterizedType")?.getToken()
        val name = it.getChildOfType("SimpleName")?.getToken()
        ParameterDefinition(type!!, name!!)
    }
}

// Would need a bit of extra handling for more complex return statements
fun Node.getReturnStatements(): ReturnStatements = ReturnStatements(
        this.findChildrenOfType("ReturnStatement")
                .map { it.getChildren().first().getToken() }
)

fun getComment(file: File): String {
    return String(file.readBytes()).substringAfterLast("/*").substringBeforeLast("*/").trim()
}

fun main(args: Array<String>) {
    val inputFile = File(INPUT_PATH)
    val tree = GumTreeJavaParser().parseFile(inputFile).root!!
    println(tree.prettyPrint())
    val methodNode = tree.getChildren()[0].getChildrenOfType("MethodDeclaration")[0]

    val methodName = methodNode.getName()
    val methodParameters = methodNode.getParameters()
    val methodReturnStatements = methodNode.getReturnStatements()
    // Comments are not part of the AST, so it's easier to extract them separately
    val comment = getComment(inputFile)

    val metadata = MethodMetadata(comment, methodName, methodParameters, methodReturnStatements)

    println(metadata)
}
