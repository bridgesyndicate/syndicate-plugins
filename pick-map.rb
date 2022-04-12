#!/usr/bin/env ruby

require 'json'
maps = JSON.parse(File.read('worlds-mainfest.json'))
map = maps.keys.sample
world = maps[map]['world_uri']
metadata = maps[map]['meta_uri']

ENV['AWS_SECRET_ACCESS_KEY'] = ENV['AWS_SECRET_KEY'] # aws needs this. Everything else is happy with AWS_SECRET_KEY
# download the maps
[world, metadata].each do |obj|
  %x/aws s3 cp #{obj} ./
  exit(-1) unless File.exists?(obj.split('/').last)
end

# echo the map name
export_cmd = <<EOD
export SYNDICATE_MAP_NAME=#{map}
export SYNDICATE_MAP_URI=#{world}
export SYNDICATE_MAP_META=#{metadata}
EOD
puts export_cmd
