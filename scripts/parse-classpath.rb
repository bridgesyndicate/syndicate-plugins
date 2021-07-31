#!/usr/bin/env ruby

foo = STDIN.read

/classpath (.+) /.match(foo)
puts $1.split(/:/)

