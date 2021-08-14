#!/usr/bin/env ruby

require 'yaml'
yaml = File.read("buildspec.yml");
obj = YAML.load(yaml)
puts 'set -e'
puts obj['phases']['pre_build']['commands']
puts obj['phases']['build']['commands']
