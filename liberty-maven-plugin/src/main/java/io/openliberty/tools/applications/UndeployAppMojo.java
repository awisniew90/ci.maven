/**
 * (C) Copyright IBM Corporation 2014, 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openliberty.tools.applications;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import net.wasdev.wlp.ant.UndeployTask;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.types.PatternSet;
import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

import io.openliberty.tools.BasicSupport;

/**
 * Undeploy application from liberty server. If no parameters have been defined
 * the mojo will undeploy all applications from the server.
 */
@Mojo(name = "undeploy")

public class UndeployAppMojo extends BasicSupport {
    
    /**
     * A file name which points to a specific module's jar | war | ear | eba |
     * zip archive.
     */
    @Parameter(property = "appArchive")
    protected String appArchive = null;
    
    /**
     * Maven coordinates of an application to undeploy. This is best listed as a
     * dependency, in which case the version can be omitted.
     */
    @Parameter
    protected ArtifactItem appArtifact;
    
    /**
     * A set of patterns to include or exclude from the undeploy operation. If
     * appArchive or appArtifact has been defined then this parameter will be
     * ignored.
     */
    @Parameter
    private PatternSet patternSet;
    
    /**
     * Timeout to verify undeploy successfully, in seconds.
     */
    @Parameter(property = "timeout", defaultValue = "40")
    protected int timeout = 40;
    
    /*
     * (non-Javadoc)
     * @see org.codehaus.mojo.pluginsupport.MojoSupport#doExecute()
     */
    @Override
    protected void doExecute() throws Exception {
        if (skip) {
            return;
        }
        
        checkServerHomeExists();
        checkServerDirectoryExists();
        
        UndeployTask undeployTask = (UndeployTask) ant
                .createTask("antlib:net/wasdev/wlp/ant:undeploy");
        
        if (undeployTask == null) {
            throw new IllegalStateException(MessageFormat.format(messages.getString("error.dependencies.not.found"), "undeploy"));
        }
        
        if (appArchive != null || appArtifact != null) {
            
            if (appArchive != null && appArtifact != null) {
                throw new MojoExecutionException(
                        messages.getString("error.app.set.twice"));
            }
            
            if (appArtifact != null) {
                Artifact artifact = getArtifact(appArtifact);
                appArchive = artifact.getFile().getName();
                
                log.info(MessageFormat.format(
                        messages.getString("info.variable.set"),
                        "artifact based application", appArtifact));
                
            } else if (appArchive != null) {
                File file = new File(appArchive);
                if (file.exists()) {
                    appArchive = file.getName();
                }
                
                log.info(MessageFormat.format(
                        messages.getString("info.variable.set"),
                        "non-artifact based application", appArchive));
                
            }
            
            File destFile = new File(serverDirectory, "dropins/" + appArchive);
            if (destFile == null || !destFile.exists()
                    || destFile.isDirectory()) {
                throw new IOException(MessageFormat.format(
                        messages.getString("error.undeploy.app.noexist"),
                        destFile.getCanonicalPath()));
            }
            
            undeployTask.setFile(appArchive);
            log.info(MessageFormat.format(
                    messages.getString("info.undeploy.app"),
                    destFile.getCanonicalPath()));
            
        } else if (patternSet != null && patternSet.hasPatterns(ant.getAnt())) {
            log.info(messages.getString("info.undeploy.patternset"));
            undeployTask.addPatternset(patternSet);
        } else {
            log.info(messages.getString("info.undeploy.all"));
        }
        
        undeployTask.setInstallDir(installDirectory);
        undeployTask.setServerName(serverName);
        undeployTask.setUserDir(userDirectory);
        undeployTask.setOutputDir(outputDirectory);
        
        // Convert from seconds to milliseconds
        undeployTask.setTimeout(Long.toString(timeout * 1000));
        undeployTask.execute();
    }
}
