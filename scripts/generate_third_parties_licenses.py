import os
import sys

MODULE_NAME = sys.argv[1]

os.chdir("../")

print("|---> GENERATING THIRD PARTIES LICENCES FILE FOR MODULE " + MODULE_NAME + "...")

os.system("./gradlew generateLicenseReport")

THIRD_PARTY_LICENSE_DESTINATION_PATH = MODULE_NAME + '/build/outputs/aar/unziped/THIRD_PARTY_LICENSES.txt'
COPY_THIRD_PARTY_LICENSE_COMMAND = "cp " + MODULE_NAME + "/build/licenses/licenses.json " + THIRD_PARTY_LICENSE_DESTINATION_PATH
os.system(COPY_THIRD_PARTY_LICENSE_COMMAND)

LICENSE_DESTINATION_PATH = MODULE_NAME + '/build/outputs/aar/unziped/LICENSE.txt'
COPY_LICENSE_COMMAND = "cp LICENSE.txt " + LICENSE_DESTINATION_PATH
os.system(COPY_LICENSE_COMMAND)

print("|---> LICENSE.txt and THIRD_PARTY_LICENSES.txt ready for aar release.")
