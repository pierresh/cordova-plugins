#!/usr/bin/env node

module.exports = function (context) {
  const xcode = require('xcode');
  const fs = require('fs');
  const path = require('path');

  const projectRoot = context.opts.projectRoot;
  const platformIos = path.join(projectRoot, 'platforms', 'ios');
  const files = fs.readdirSync(platformIos).filter(e => e.endsWith('.xcodeproj'));

  if (files.length === 0) {
    console.log('No Xcode project found in ios platform.');
    return;
  }

  const xcodeproj = path.join(platformIos, files[0], 'project.pbxproj');
  const project = xcode.project(xcodeproj);
  project.parseSync();

  const configurations = project.pbxXCBuildConfigurationSection();

  for (const config in configurations) {
    const buildSettings = configurations[config].buildSettings;
    if (buildSettings) {
      buildSettings.SWIFT_VERSION = '5.0';
      buildSettings.IPHONEOS_DEPLOYMENT_TARGET = '13.0';
      buildSettings.LD_RUNPATH_SEARCH_PATHS = '"@executable_path/Frameworks"';
    }
  }

  fs.writeFileSync(xcodeproj, project.writeSync());
  console.log('✅ Swift 5 support and runpath search paths updated in Xcode project');
};
