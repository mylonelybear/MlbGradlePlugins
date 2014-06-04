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
            resource
        }

        project.task('clean') << {
            project.delete project.packer.buildDir
        }

        project.task('prepare') {
            outputs.dir project.packer.baseBuildDir
            outputs.dir project.packer.resourcesBuildDir

            inputs.files project.configurations.base
            inputs.files project.configurations.resource
            inputs.dir project.packer.resourcesDir


            doLast {
                def baseZip = project.file(project.configurations.base.asPath)
                def resourcesZip = project.file(project.configuraitons.resource.asPath)

                project.copy {
                    from project.zipTree(baseZip)
                    into packer.baseBuildDir
                }

                project.file(packer.resourcesBuildDir).mkdirs()

                project.copy {
                    from project.zipTree(resourcesZip)
                    into packer.resourcesBuildDir
                }

                project.copy {
                    from project.file(packer.resourcesDir)
                    into packer.resourcesBuildDir
                }
            }
        }

        project.task('build', dependsOn: project.prepare) {
            inputs.file project.prepare.outputs.files
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
    def baseBuildDir = "$buildDir/base"
    def boxBuildDir = "$buildDir/box"
    def resourcesBuildDir = "$buildDir/resources"

    def resourcesDir = 'src/main/resources'
    def srcDir = 'src/main/packer'

    def distDir = 'dist'
}


