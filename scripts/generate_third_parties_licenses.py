import os

os.chdir("../")
print("|---> GENERATING THIRD PARTIES LICENCES FILE ...")

os.system("./gradlew generateLicenseReport")
os.system("cp collaboration-suite-utils/build/licenses/licenses.json .")
os.system("mv licenses.json THIRD_PARTY_LICENSES.json")

print("|---> GENERATED THIRD PARTIES LICENCES FILE to THIRD_PARTY_LICENSES.txt")
