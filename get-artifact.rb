#!/usr/bin/env ruby

BUCKET = 'syndicate-plugins-artifacts'.freeze

src = ARGV[0]
dst = ARGV[1]

src_file = src.split(%r{/}).last

md5_hash = {
  'craftbukkit-1.8.8-R0.1-SNAPSHOT.jar' => '4b58fd26f4236448c03361750de246fc',
  'MockBukkit.jar' => 'b0b6588024e558ce36a64df9e71f33c0',
  'ProtocolLib.jar' => '8e568aeaa6ac7a357cd95cfc07df5cf9',
  'cage.json' => '5cdd9c54bc06f156e2739a473f0dda5c',
  'jar-deps.tar' => '57238a1b4c5d733b200fdb9db9832828',
}

DST_IS_DIR = Dir.exist?(dst)

md5_target = DST_IS_DIR ? File.join([dst, src_file]) : dst

md5_sum = `md5sum #{md5_target}`.split(/\s+/).first

hash_index = DST_IS_DIR ? src_file : dst.gsub('./','')

unless md5_sum == md5_hash[hash_index]
  cmd = "aws s3 cp s3://#{BUCKET}/#{src} #{dst}"
  puts "get-artifact.rb: #{cmd}"
  `#{cmd}`
else
  puts "get-artifact.rb: skip #{src_file}"
end

