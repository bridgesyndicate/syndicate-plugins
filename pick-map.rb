#!/usr/bin/env ruby

require 'json'
maps = JSON.parse(File.read('worlds-mainfest.json'))
map = maps.keys.sample
world = maps[map]['world_uri']
metadata = maps[map]['meta_uri']

# download the maps
%x/aws s3 cp #{world} ./
%x/aws s3 cp #{metadata} ./

# echo the map name
export_cmd = <<EOD
export SYNDICATE_JAVA_OPTS="-DmapName=#{map}"
EOD
puts export_cmd
