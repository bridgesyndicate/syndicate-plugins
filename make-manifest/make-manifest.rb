#!/usr/bin/env ruby
require 'bundler'
Bundler.require

def bucket_uri
  "s3://#{BUCKET}"
end

BUCKET='syndicate-maps'
s3 = Aws::S3::Client.new
s3retval = s3.list_objects({ bucket: BUCKET })

manifest = {}
s3retval.contents.each do |object|
  if %r{/$}.match(object.key)
    manifest[object.key.chop] = {
      world_uri: "#{bucket_uri}/world.tar.gz",
      meta_uri: "#{bucket_uri}/meta.json"
    }
  end
end

puts JSON.pretty_generate(manifest)
