#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.

import argparse
import json
import os
import sys
from pathlib import Path

script_path = Path(os.path.dirname(os.path.realpath(sys.argv[0])))
base_path = script_path.parent.absolute()
data_dir = base_path / "distribution" / "data"
target_dir = base_path / "target" / "data"

parser = argparse.ArgumentParser(description='Prepare the data for upload to server')
parser.add_argument('-v', '--version', dest='version', required=True)
parser.add_argument('-r', '--resources', dest='resources', required=True)

args = parser.parse_args()


def tar_file(source, target):
    cmd = f"cd {source.parent} && tar czf {target} {source.relative_to(source.parent)}"
    if source.is_dir():
        cmd += "/*"
    else:
        cmd += ".model.bin"
    print(f"Packaging {source.name}")
    os.system(cmd)


def get_source_and_target(language, name, target):
    source_file = None
    target_file = target_dir / f"{language.lower()}" / f"{args.version}" / f"{name}.tar.gz"
    if target.lower() == "lexicons":
        source_file = "lexicons/"
    else:
        source_file = f"models/{name}"
    return source_file, target_file


if not target_dir.absolute().exists():
    target_dir.absolute().mkdir(parents=True)

manifest_json = data_dir / "manifest.json"
os.system(f"cp {manifest_json} {target_dir}")

with manifest_json.open() as fp:
    manifest = json.load(fp)

for name, language in manifest["languages"].items():
    print(f"Processing version {args.version} for {name}")

    packages_json = data_dir / f"{language.lower()}" / "packages.json"
    releases_json = data_dir / f"{language.lower()}" / "releases.json"

    language_dir = target_dir / f"{language.lower()}"
    if not language_dir.absolute().exists():
        language_dir.absolute().mkdir(parents=True)

    os.system(f"cp {packages_json} {language_dir}")
    os.system(f"cp {releases_json} {language_dir}")

    with releases_json.open() as fp:
        releases = json.load(fp)

    resources_dir = Path(args.resources) / f"{language.lower()}"

    models = list()
    for key, v in releases.items():
        source_file = None
        target_file = None

        if "variants" in v:
            for sk, sv in v["variants"].items():
                if args.version in sv["versions"]:
                    source_file, target_file = get_source_and_target(language, f"{key}-{sk}", v["target"])
        elif args.version in v["versions"]:
            source_file, target_file = get_source_and_target(language, key, v["target"])

        if source_file and target_file:
            source_file = (resources_dir / source_file).absolute()
            tar_file(source_file, target_file)
