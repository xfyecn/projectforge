/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2019 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.start;

import org.apache.commons.lang3.StringUtils;
import org.projectforge.ProjectForgeApp;
import org.projectforge.common.LoggerSupport;
import org.projectforge.framework.time.DateHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.ConnectorStartFailedException;
import org.springframework.boot.web.servlet.ServletComponentScan;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TimeZone;

@SpringBootApplication(
        scanBasePackages = {"org.projectforge", "de.micromata.mgc.jpa.spring"},
        // Needed for Spring 5.5, exception was:
        // java.lang.ClassCastException: org.springframework.orm.jpa.EntityManagerHolder cannot be cast to org.springframework.orm.hibernate5.SessionHolder
        exclude = HibernateJpaAutoConfiguration.class
)
@ServletComponentScan({"org.projectforge.web", "org.projectforge.business.teamcal.servlet"})
public class ProjectForgeApplication {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProjectForgeApplication.class);

  private static final String ADDITIONAL_LOCATION_ARG = "--spring.config.additional-location=";

  public static final String PROPERTIES_FILENAME = "projectforge.properties";

  private static final String ENV_PROJECTFORGE_HOME = "PROJECTFORGE_HOME";

  private static final String COMMAND_LINE_VAR_HOME_DIR = "home.dir";

  private static final String[] DIR_NAMES = {"ProjectForge", "Projectforge", "projectforge"};

  public static void main(String[] args) {
    String javaVersion = System.getProperty("java.version");
    if (javaVersion != null && javaVersion.compareTo("1.9") >= 0) {
      new LoggerSupport(log, LoggerSupport.Priority.VERY_IMPORTANT)
              .log("ProjectForge doesn't support versions higher than Java 1.8!!!!")
              .log("")
              .log("Please downgrade. Sorry, we're working on newer Java versions.")
              .logEnd();
    }
    String param = ProjectForgeApp.CONFIG_PARAM_BASE_DIR;
    String appHomeDir = System.getProperty(param); // Might be defined as -Dprojectforge.base.dir
    if (StringUtils.isBlank(appHomeDir)) {
      param = COMMAND_LINE_VAR_HOME_DIR;
      appHomeDir = System.getProperty(param); // Might be defined as -Dhome.dir=....
    }
    if (StringUtils.isNotBlank(appHomeDir)) {
      log.info("Trying ProjectForges base dir as given at commandline -D" + param + "=" + appHomeDir);
    } else {
      appHomeDir = System.getenv(ENV_PROJECTFORGE_HOME); // Environment variable.
      if (StringUtils.isNotBlank(appHomeDir)) {
        log.info("Trying ProjectForges base dir as given as system environment variable $" + ENV_PROJECTFORGE_HOME + ": " + appHomeDir);
        if (!ProjectForgeHomeFinder.checkDirectory(new File(appHomeDir), false)) {
          log.error("Directory '" + appHomeDir + "' configured as environment variable not found. Create this directory or unset the environment variable $" + ENV_PROJECTFORGE_HOME + ".");
        }
      }
    }
    File baseDir = StringUtils.isNotBlank(appHomeDir) ? new File(appHomeDir) : null;
    if (baseDir != null && !ProjectForgeHomeFinder.checkDirectory(baseDir, false)) {
      log.error("The configured base directory doesn't exist or isn't a directory, giving up :-(");
      ProjectForgeHomeFinder.giveUp();
    }
    if (baseDir == null) {
      // Try to find ProjectForge in current directory:
      baseDir = ProjectForgeHomeFinder.findBaseDir(new File("."));
      if (baseDir == null) {
        // No ProjectForge base directory found. Assuming current directory.
        baseDir = new File("ProjectForge");
        if (ProjectForgeHomeFinder.isProjectForgeSourceCodeRepository(baseDir)) {
          log.error("Found '" + baseDir + "' as ProjectForge directory. This seems to be the source code repository. Can't use it as ProjectForge home dir.");
          ProjectForgeHomeFinder.giveUp();
        }
        log.info("No previous ProjectForge installation found (searched for ./ProjectForge and $HOME/ProjectForge). Trying to create a new base directory: " + baseDir.getAbsolutePath());
        String msg = "Do you allow ProjectForge to create and initialize the directory (y/n) '" + baseDir.getAbsolutePath() + "'?";
        String answer = new ConsoleTimeoutReader(msg).ask();
        if (!StringUtils.startsWith(answer, "y")) {
          // No permission by the user in console input or time out:
          log.error("Creation of directory '" + baseDir.getAbsolutePath() + "' aborted, giving up :-(");
          ProjectForgeHomeFinder.giveUp();
        } else if (!baseDir.mkdir()) {
          log.error("Creation of directory '" + baseDir.getAbsolutePath() + "' failed, giving up :-(");
          ProjectForgeHomeFinder.giveUp();
        }
      }
    }
    new LoggerSupport(log, LoggerSupport.Priority.HIGH)
            .log("Using ProjectForge directory: " + baseDir.getAbsolutePath())
            .logEnd();
    System.setProperty(ProjectForgeApp.CONFIG_PARAM_BASE_DIR, baseDir.getAbsolutePath());
    if (!new File(baseDir, PROPERTIES_FILENAME).exists()) {
      new LoggerSupport(log)
              .log("Creating new ProjectForge installation!")
              .log("")
              .log(baseDir.getAbsolutePath())
              .logEnd();
    }
    ProjectForgeApp.ensureInitialConfigFile("initialProjectForge.properties", PROPERTIES_FILENAME);
    args = addDefaultAdditionalLocation(baseDir, args);
    System.setProperty("user.timezone", "UTC");
    TimeZone.setDefault(DateHelper.UTC);
    try {
      SpringApplication.run(ProjectForgeApplication.class, args);
    } catch (Exception ex) {
      log.error("Exception while running application: " + ex.getMessage(), ex);
      LoggerSupport loggerSupport = new LoggerSupport(log, LoggerSupport.Priority.VERY_IMPORTANT, LoggerSupport.Alignment.LEFT)
              .setLogLevel(LoggerSupport.LogLevel.ERROR)
              .log("Error while running application:")
              .log("")
              .log(ex.getMessage());
      if (ex instanceof ConnectorStartFailedException) {
        loggerSupport.log("")
                .log("May-be address of server port is already in use.");
      }
      loggerSupport.logEnd();
      throw ex;
    }
  }

  /**
   * For easier configuration, ProjectForge adds ${user.home}/ProjectForge/projectforge.properties as
   * --spring.config.additional-location.
   * <br/>
   * If the additional-location is already given in the args, nothing will done by this method.
   *
   * @param args The main args.
   * @return The main args extended by the new --spring.config.additional-location if not already given.
   */
  static String[] addDefaultAdditionalLocation(File baseDir, String[] args) {
    checkConfiguration("config", "application-default.properties");
    checkConfiguration("config", "application.properties");
    checkConfiguration(".", "application-default.properties");
    checkConfiguration(".", "application.properties");
    if (baseDir == null || !checkConfiguration(baseDir.getAbsolutePath(), PROPERTIES_FILENAME)) {
      return args;
    }
    // Add found projectforge.properties as additional arg (Spring uses this file with highest priority for configuration).
    boolean additionalLocationFound = false;
    if (args == null || args.length == 0) {
      args = new String[]{getAddtionalLocationArg(baseDir)};
    } else {
      for (String arg : args) {
        if (arg != null && arg.startsWith(ADDITIONAL_LOCATION_ARG)) {
          additionalLocationFound = true;
          break;
        }
      }
      if (!additionalLocationFound) {
        args = Arrays.copyOf(args, args.length + 1);
        args[args.length - 1] = getAddtionalLocationArg(baseDir);
      }
    }
    return args;
  }

  /**
   * $HOME is replaced by the user's home.
   *
   * @return --spring.config.additional-location=file:/$HOME/ProjectForge/projectforge.properties
   */
  static String getAddtionalLocationArg(File dir) {
    return ADDITIONAL_LOCATION_ARG + "file:" + getAddtionLocation(dir).getAbsolutePath();
  }

  /**
   * @param baseDir Used base dir to find projectforge.properties. If not given, the user's home dir will be used.
   * @return The projectforge.properties file in path baseDir or user's home dir.
   */
  static File getAddtionLocation(File baseDir) {
    return baseDir != null ? new File(baseDir, PROPERTIES_FILENAME)
            : Paths.get(System.getProperty("user.home"), "ProjectForge", PROPERTIES_FILENAME).toFile();
  }

  private static boolean checkConfiguration(String dir, String filename) {
    if (dir == null)
      return false;
    File file = new File(dir, filename);
    if (file.exists()) {
      log.info("Configuration from '" + file.getAbsolutePath() + "' will be used.");
      return true;
    }
    return false;
  }
}
