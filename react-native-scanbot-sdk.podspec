require 'json'
package = JSON.parse(File.read('package.json'))

Pod::Spec.new do |s|

  s.name           = "react-native-scanbot-sdk"
  s.version        = package["version"]
  s.summary        = "Scanbot SDK for React Native."
  s.homepage       = package["homepage"]
  s.author         = { "doo GmbH" => "joel@oblador.se" }
  s.ios.deployment_target = '7.0'
  s.license        = "Proprietary"
  s.source_files   = 'ios/**/*.{h,m}'
  s.resources      = 'ios/images/*.png'
  s.source         = { :git => "https://github.com/oblador/react-native-keychain.git", :tag => "v#{s.version}" }
  s.dependency 'React'
  s.dependency 'ScanbotSDK', '~> 1.6.0'
end
