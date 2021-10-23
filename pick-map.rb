#!/usr/bin/env ruby

require 'json'
maps = JSON.parse(File.read('./world-manifest.json'))
map = maps.keys.sample
world = maps[map]['world_uri']
metadata = maps[map]['meta_uri']
puts "aws s3 cp #{world} ."
puts "aws s3 cp #{metadata} ."
puts 'export SYNDICATE_JAVA_OPTS="--map-name ' + map + '"'
