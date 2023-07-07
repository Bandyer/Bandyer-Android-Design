import os
import sys

MODULE_NAME = sys.argv[1]

os.chdir("../")

print("|---> GENERATING THIRD PARTIES LICENCES FILE FOR MODULE " + MODULE_NAME + "...")

def replaceTextInFile(path, match, replace):
    with open(path) as file:
        text = file.read().replace(match, replace)
    with open(path, "w") as writer:
        writer.write(text)

## temporary implement core-ui module as compileOnly for licenses generation
os.chdir(MODULE_NAME)
replaceTextInFile("build.gradle", "api project(\":collaboration-suite-core-ui\")", "compileOnly project(\":collaboration-suite-core-ui\")")

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
replaceTextInFile("build.gradle", "compileOnly project(\":collaboration-suite-core-ui\")", "api project(\":collaboration-suite-core-ui\")")

print("|---> LICENSE.txt and THIRD_PARTY_LICENSES.txt ready for aar release.")
