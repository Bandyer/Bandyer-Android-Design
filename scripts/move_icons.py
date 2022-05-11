import glob
import os
import re
import requests
import sys
import shutil
import lxml.etree


ZEPLIN_TOKEN = sys.argv[1]
ZEPLIN_STYLE_GUIDES = sys.argv[2]
PROJECT_NAME = sys.argv[3]

drawables = []
SOURCE_FOLDER = '../img/unused_drawables/'
DESTINATION_FOLDER = '../' + PROJECT_NAME + '/src/main/res/drawable/'
DESTINATION_LINK_FILE = '../' + PROJECT_NAME + '/src/main/res/values/res.xml'
FILE_PREFIX = 'kaleyra_z_'

if not os.path.isfile(DESTINATION_LINK_FILE): sys.exit();

def remove_prefix(text, prefix):
    return text[text.startswith(prefix) and len(prefix):]

tree = lxml.etree.parse(DESTINATION_LINK_FILE)
root = tree.getroot()
for child in root:
    if not "@drawable" in child.text: continue
    drawables.append(remove_prefix(child.text,"@drawable/"))

for file in os.listdir(SOURCE_FOLDER):
    src = SOURCE_FOLDER+file
    dst = DESTINATION_FOLDER+file
    f = file.split('.')[0]
    if f in drawables:
       shutil.move(src,dst)