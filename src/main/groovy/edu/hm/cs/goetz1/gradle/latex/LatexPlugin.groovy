package edu.hm.cs.goetz1.gradle.latex

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete

class LatexPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create("latex", LatexPluginExtension)
        
        def pdflatexFirstRun = project.tasks.create("pdflatexFirstRun", Exec.class)
        pdflatexFirstRun.setDescription("Produces a pdf from a tex document by executing pdflatex once.")
        pdflatexFirstRun.setGroup("LaTeX")
        
        def biber = project.tasks.create("biber", Exec.class)
        biber.setDescription("Runs biber to process the bibliography.")
        biber.setGroup("LaTeX")
        
        project.afterEvaluate {
            pdflatexFirstRun.commandLine "pdflatex", "-output-directory=../${project.latex.outputDir}", 
                        "-synctex=1", "-interaction=nonstopmode", 
                        "${project.latex.mainFilename}.tex"
            pdflatexFirstRun.workingDir("${project.latex.sourceDir}")
            pdflatexFirstRun.inputs.dir("${project.latex.sourceDir}")
            pdflatexFirstRun.outputs.dir("${project.latex.outputDir}")
            
            biber.commandLine "biber", "${project.latex.mainFilename}"
            biber.workingDir("${project.latex.outputDir}")
            biber.inputs.file("${project.latex.outputDir}/${project.latex.mainFilename}.aux")
            biber.outputs.files("${project.latex.outputDir}/${project.latex.mainFilename}.bbl", 
                    "${project.latex.outputDir}/${project.latex.mainFilename}.blg")
        }
    }
}


