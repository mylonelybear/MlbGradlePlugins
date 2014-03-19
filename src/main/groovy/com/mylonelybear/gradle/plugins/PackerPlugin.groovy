package com.mylonelybear.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.api.publish.ivy.IvyPublication

import org.gradle.api.tasks.bundling.Zip

class PackerPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('packer', PackerPluginExtension)

        project.configurations {
            base
        }

        project.task('clean') << {
            project.delete project.packer.buildDir
        }

        project.task('build') {
            inputs.file project.packer.srcDir
            outputs.dir project.packer.buildDir

            doLast {
                def srcFile = project.file("${project.packer.srcDir}/${project.name}.json")

                project.copy {
                    from srcFile
                    into project.packer.buildDir
                    expand(project: project)
                }

                project.delete project.packer.boxBuildDir

                project.exec {
                    executable 'packer'
                    args 'build', "${project.packer.buildDir}/${srcFile.name}"
                }
            }
        }

        project.task('dist', type: Zip, dependsOn: project.build) {
            baseName = project.name
            from project.packer.boxBuildDir
            destinationDir = project.file(project.packer.distDir)
        }

        project.publish.dependsOn project.dist

        project.publishing {
            publications {
                ivy (IvyPublication) {
                    artifact project.dist
                }
            }
        }
    }
}

class PackerPluginExtension {
    def buildDir = 'build/packer'
    def boxBuildDir = "$buildDir/box"

    def srcDir = 'src/main/packer'

    def distDir = 'dist'
}


