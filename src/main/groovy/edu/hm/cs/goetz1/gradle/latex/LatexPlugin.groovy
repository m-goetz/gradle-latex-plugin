package edu.hm.cs.goetz1.gradle.latex

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import java.io.ByteArrayOutputStream
import org.gradle.api.logging.LogLevel

class LatexPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create("latex", LatexPluginExtension)
        
        def pdflatexFirstRun = project.tasks.create("pdflatexFirstRun", Exec.class)
        pdflatexFirstRun.setDescription("Produces a pdf from a tex document by executing pdflatex once.")
        pdflatexFirstRun.setGroup("LaTeX")
        
        def biber = project.tasks.create("biber", Exec.class)
        biber.setDescription("Runs biber to process the bibliography.")
        biber.setGroup("LaTeX")
        biber.dependsOn(pdflatexFirstRun)
        
        def pdflatex = project.tasks.create("pdflatex", Exec.class)
        pdflatex.setDescription("Produces the final pdf from a tex document by executing pdflatex after biber.")
        pdflatex.setGroup("LaTeX")
        pdflatex.dependsOn(biber)
        
        project.afterEvaluate {
            def outputDirectory = new File("${project.rootDir}/${project.latex.outputDir}")
            outputDirectory.mkdirs()
            
            pdflatexFirstRun.commandLine "pdflatex", 
                        "-output-directory=${outputDirectory}", 
                        "-synctex=1", "-interaction=nonstopmode", 
                        "${project.latex.mainFilename}.tex"
            pdflatexFirstRun.workingDir("${project.latex.sourceDir}")
            pdflatexFirstRun.inputs.dir("${project.latex.sourceDir}")
            pdflatexFirstRun.outputs.file("${outputDirectory}/${project.latex.mainFilename}.bcf")
            pdflatexFirstRun.logging.captureStandardOutput LogLevel.INFO
            pdflatexFirstRun.logging.captureStandardError  LogLevel.ERROR
            
            def inputFile = new File("${outputDirectory}/${project.latex.mainFilename}.bcf")
            biber.commandLine "biber", "--output_directory=${outputDirectory}", 
                        "--logfile=${project.latex.mainFilename}","${inputFile}"
            biber.workingDir("${project.latex.sourceDir}")
            biber.inputs.file("${outputDirectory}/${project.latex.mainFilename}.bcf")
            biber.outputs.files("${outputDirectory}/${project.latex.mainFilename}.bbl", 
                    "${outputDirectory}/${project.latex.mainFilename}.blg")
            biber.logging.captureStandardOutput LogLevel.INFO
            biber.logging.captureStandardError  LogLevel.ERROR
            biber.onlyIf { inputFile.exists() }
            
            pdflatex.commandLine "pdflatex", 
                        "-output-directory=${outputDirectory}", 
                        "-synctex=1", "-interaction=nonstopmode", 
                        "${project.latex.mainFilename}.tex"
            pdflatex.workingDir("${project.latex.sourceDir}")
            pdflatex.inputs.dir("${project.latex.sourceDir}")
            pdflatex.outputs.dir("${outputDirectory}")
            pdflatex.logging.captureStandardOutput LogLevel.INFO
            pdflatex.logging.captureStandardError  LogLevel.ERROR
        }
    }
}


