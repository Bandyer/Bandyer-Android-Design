import glob
import os
import re
import requests
import sys

ZEPLIN_TOKEN = sys.argv[1]
ZEPLIN_STYLE_GUIDES = sys.argv[2]
PROJECT_NAME = sys.argv[3]
SOURCE_FOLDER = '../img/assets/'
DESTINATION_FOLDER = '../' + PROJECT_NAME + '/src/main/res/drawable/'
FILE_PREFIX = 'kaleyra_z_'


def camel_to_snake(name):
    return re.sub('[^0-9a-zA-Z]+', '_', name).lower()


def clean_folders():
    for f in glob.glob('../img/assets/*'):
        os.remove(f)
    for f in glob.glob(DESTINATION_FOLDER + FILE_PREFIX + '*'):
        os.remove(f)


clean_folders()

base_url = "https://api.zeplin.dev/v1/styleguides/" + ZEPLIN_STYLE_GUIDES + "/components"

payload = {}
headers = {
    'Accept': 'application/json',
    'Authorization': 'Bearer ' + ZEPLIN_TOKEN
}

response = requests.request("GET", base_url + "?sort=section&limit=100&offset=0", headers=headers, data=payload)

jsonResponse = response.json()

for images in jsonResponse:
    id = images['id']
    name = FILE_PREFIX + camel_to_snake(images['name'])
    urls = images['image']['original_url']

    svg_url = requests.request("GET", base_url + "/" + id + "/versions/latest", headers=headers, data=payload).json()['assets'][0]['contents'][5]['url']
    response = requests.get(svg_url)
    svg_path = SOURCE_FOLDER + name + ".svg"
    file = open(svg_path, "wb")
    file.write(response.content)
    file.close()
    os.system('SVGtoVD/vd-tool -c -in ' + SOURCE_FOLDER + ' -out ' + DESTINATION_FOLDER)
