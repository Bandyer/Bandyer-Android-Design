import os
import sys

MODULE_NAME = sys.argv[1]

os.chdir("../")

print("|---> GENERATING THIRD PARTIES LICENCES FILE FOR MODULE " + MODULE_NAME + "...")

## temporary implement core-ui module as compileOnly for licenses generation
os.chdir(MODULE_NAME)
with open("build.gradle") as buildGradle:
  text = buildGradle.read().replace("api project(\":collaboration-suite-core-ui\")", "compileOnly project(\":collaboration-suite-core-ui\")")
with open("build.gradle", "w") as writer:
  writer.write(text)

os.chdir("../")
os.system("./gradlew :" + MODULE_NAME + ":generateLicenseReport")

## copy third party licenses file into releasing unziped folder
THIRD_PARTY_LICENSE_DESTINATION_PATH = MODULE_NAME + '/build/outputs/aar/unziped/THIRD_PARTY_LICENSES.txt'
COPY_THIRD_PARTY_LICENSE_COMMAND = "cp " + MODULE_NAME + "/build/licenses/licenses.json " + THIRD_PARTY_LICENSE_DESTINATION_PATH
os.system(COPY_THIRD_PARTY_LICENSE_COMMAND)

## copy license file into releasing unziped folder
LICENSE_DESTINATION_PATH = MODULE_NAME + '/build/outputs/aar/unziped/LICENSE.txt'
COPY_LICENSE_COMMAND = "cp LICENSE.txt " + LICENSE_DESTINATION_PATH
os.system(COPY_LICENSE_COMMAND)

## reset implementation of core-ui module as api for licenses generation
os.chdir(MODULE_NAME)
with open("build.gradle") as resetBuildGradle:
  resetText = resetBuildGradle.read().replace("compileOnly project(\":collaboration-suite-core-ui\")", "api project(\":collaboration-suite-core-ui\")")
with open("build.gradle", "w") as resetWriter:
  resetWriter.write(resetText)

print("|---> LICENSE.txt and THIRD_PARTY_LICENSES.txt ready for aar release.")
