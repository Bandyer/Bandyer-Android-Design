import re
import requests
import sys
import os

ZEPLIN_TOKEN = sys.argv[1]
ZEPLIN_STYLE_GUIDES = sys.argv[2]
PROJECT_NAME = sys.argv[3]


def camel_to_snake(name):
    return re.sub('[^0-9a-zA-Z]+', '_', name).lower()


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
    name = 'bandyer_z_' + camel_to_snake(images['name'])
    urls = images['image']['original_url']

    svg_url = requests.request("GET", base_url + "/" + id + "/versions/latest", headers=headers, data=payload).json()['assets'][0]['contents'][5]['url']
    response = requests.get(svg_url)
    svg_path = '../img/assets/' + name + ".svg"
    file = open(svg_path, "wb")
    file.write(response.content)
    file.close()
    os.system('SVGtoVD/vd-tool -c -in ../img/assets -out ../'+PROJECT_NAME+'/src/main/res/drawable')

