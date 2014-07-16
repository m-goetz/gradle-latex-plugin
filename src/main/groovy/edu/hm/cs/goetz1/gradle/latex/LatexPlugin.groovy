package edu.hm.cs.goetz1.gradle.latex

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import java.io.ByteArrayOutputStream
import org.gradle.api.logging.LogLevel

class LatexPlugin implements Plugin<Project> {
    
    Project project
    
    Task pdflatexPreBuild
    Task biber
    Task pdflatex
    Task cleanPdflatex
    
    File outputDirectory

    void apply(Project project) {
        project.extensions.create("latex", LatexPluginExtension)
        this.project = project
        createAllTasks()
        
        project.afterEvaluate {
            ensureExistingOutputDirectory()
            ensureExistingMainFilename()
            
            pdflatexPreBuild.configure pdflatexPreBuildConfig
            pdflatexPreBuild.configure standardLogging
            
            biber.configure biberConfig
            biber.configure standardLogging
            
            pdflatex.configure pdflatexConfig
            pdflatex.configure standardLogging
            
            cleanPdflatex.delete outputDirectory.listFiles()
        }
    }
    
    private void createAllTasks() {
        pdflatexPreBuild = createLatexTask("pdflatexPreBuild", 
                "Produces a pdf from a latex document by executing pdflatex once.")
        biber = createLatexTask("biber", 
                "Runs biber to process the bibliography entries in the latex document.")
        biber.dependsOn(pdflatexPreBuild)
        pdflatex = createLatexTask("pdflatex", 
                "Produces the final pdf from a latex document by executing pdflatex a second time.")
        pdflatex.dependsOn(biber)    
        cleanPdflatex = project.tasks.create("cleanPdflatex", Delete.class)
        cleanPdflatex.setDescription("Deletes the output of the pdflatex task.")
        cleanPdflatex.setGroup("LaTeX")
    }
    
    private Task createLatexTask(String name, String description) {
        def latexTask = project.tasks.create(name, Exec.class)
        latexTask.setDescription(description)
        latexTask.setGroup("LaTeX")     
        return latexTask
    }
    
    private void ensureExistingOutputDirectory() {
        outputDirectory = new File("${project.projectDir}/${project.latex.outputDir}")
        outputDirectory.mkdirs()
    }
    
    private void ensureExistingMainFilename() {
        if (project.latex.mainFilename == null) {
            project.latex.mainFilename = "${project.name}"
        }
    }
    
    private final Closure pdflatexPreBuildConfig = {
        commandLine "pdflatex", 
                        "-output-directory=${outputDirectory}", 
                        "-synctex=1", "-interaction=nonstopmode", 
                        "${project.latex.mainFilename}.tex"
        workingDir("${project.latex.sourceDir}")
        inputs.dir("${project.latex.sourceDir}")
        outputs.file("${outputDirectory}/${project.latex.mainFilename}.bcf")
    }
    
    private final Closure biberConfig = {
        def inputFile = new File("${outputDirectory}/${project.latex.mainFilename}.bcf")
        commandLine "biber", "--output_directory=${outputDirectory}", 
                        "--logfile=${project.latex.mainFilename}","${inputFile}"
        workingDir("${project.latex.sourceDir}")
        inputs.file("${outputDirectory}/${project.latex.mainFilename}.bcf")
        outputs.files("${outputDirectory}/${project.latex.mainFilename}.bbl", 
                    "${outputDirectory}/${project.latex.mainFilename}.blg")
        onlyIf { inputFile.exists() }
    }
    
    private final Closure pdflatexConfig = {
        commandLine "pdflatex", 
                        "-output-directory=${outputDirectory}", 
                        "-synctex=1", "-interaction=nonstopmode", 
                        "${project.latex.mainFilename}.tex"
        workingDir("${project.latex.sourceDir}")
        inputs.dir("${project.latex.sourceDir}")
        outputs.dir("${outputDirectory}")
    }
    
    private final Closure standardLogging = {
        logging.captureStandardOutput LogLevel.INFO
        logging.captureStandardError  LogLevel.ERROR
    }
}


