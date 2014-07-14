package edu.hm.cs.goetz1.gradle.latex

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete

class LatexPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.extensions.create("latex", LatexPluginExtension)
        
        def pdfLatexExecTask = project.tasks.create("basicPdfLatex", Exec.class)
        pdfLatexExecTask.setDescription("Produces a pdf from a tex document by executing pdflatex once.")
        pdfLatexExecTask.setGroup("LaTeX")
        
        def biberExecTask = project.tasks.create("biber", Exec.class)
        biberExecTask.setDescription("Executes biber to process the biblography.")
        biberExecTask.setGroup("LaTeX")
        
        def pdflatexTask = project.tasks.create("pdflatex", Exec.class)
        pdflatexTask.setDescription("Produces the final pdf from a tex document by executing pdflatex after biber.")
        pdflatexTask.setGroup("LaTeX")
        
        def pdfLatexConfigTask = project.task("configPdfLatex") << {
            def outputDirectory = new File("${project.latex.outputDir}/pdflatex")
            outputDirectory.mkdirs();
            def inputFile = new File("${project.latex.mainFilename}.tex")
            pdfLatexExecTask.configure {
                commandLine("pdflatex", "-synctex=1", "-interaction=nonstopmode", 
                    "-output-directory=${outputDirectory}", "${inputFile}")
                outputs.dir("${outputDirectory}")
                inputs.files("${inputFile}")
            }
        }
        
        def biberConfigTask = project.task("configBiber") << {
            def outputDirectory = new File("${project.latex.outputDir}/biber")
            outputDirectory.mkdirs();
            def inputFile = new File("${project.latex.outputDir}/pdflatex/${project.latex.mainFilename}.bcf")
            biberExecTask.configure {
                commandLine("biber", "--output_directory=${outputDirectory}", 
                        "--logfile=${project.latex.mainFilename}","${inputFile}")
                outputs.dir("${outputDirectory}")
                inputs.files("${inputFile}")
            }
        }
        
        def copyTask = project.tasks.create("copyBblFile", Copy.class)
        copyTask.configure {
            from "${project.latex.outputDir}/biber"
            into "${project.latex.outputDir}/pdflatex"
            include "*.bbl"
        }
        
        def finalPdflatexConfigTask = project.task("configFinalPdflatex") << {
            def outputDirectory = new File("${project.latex.outputDir}/pdflatex")
            outputDirectory.mkdirs();
            def inputFile = new File("${project.latex.mainFilename}.tex")
            pdflatexTask.configure {
                commandLine("pdflatex", "-synctex=1", "-interaction=nonstopmode", 
                    "-output-directory=${outputDirectory}", "${inputFile}")
                outputs.dir("${outputDirectory}")
                inputs.files("${inputFile}")
            }
        }
        
        def deleteBblTask = project.tasks.create("deleteBblFile", Delete.class)
        deleteBblTask.configure {
            delete "${project.latex.outputDir}/pdflatex/${project.latex.mainFilename}.bbl"
        }
        
        pdflatexTask << {
            println deleteBblTask.targetFiles
        }

        pdfLatexExecTask.dependsOn(pdfLatexConfigTask)
        biberExecTask.dependsOn(biberConfigTask)
        biberExecTask.dependsOn(pdfLatexExecTask)
        pdflatexTask.dependsOn(biberExecTask)
        pdflatexTask.dependsOn(finalPdflatexConfigTask)
        finalPdflatexConfigTask.dependsOn(copyTask)
        pdflatexTask.finalizedBy(deleteBblTask)
    }
}

class LatexPluginExtension {
    String mainFilename = "main"
    String outputDir = "build"
}
