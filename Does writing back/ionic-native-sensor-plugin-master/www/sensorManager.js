var DeviceSensorLoader = function(require, exports, module) {
  var exec = require('cordova/exec');

  var intervalId;
  var intervalId2;
  
  function DeviceSensor() {}

  DeviceSensor.prototype.start = function(success, failure, timeOffset) {
    exec(success, failure, 'AndroidSensorManager', 'start', []);
    intervalId = setInterval(function() {
      exec(success, failure, 'AndroidSensorManager', 'getCurrent', []);
    }, timeOffset || 500);
  };

  DeviceSensor.prototype.stop = function(success, failure) {
    exec(success, failure, 'AndroidSensorManager', 'stop', []);
    intervalId = setInterval(function() {
      exec(success, failure, 'AndroidSensorManager', 'getDin', []);
    }, 500);
  };

  // DeviceSensor.prototype.getDin = function(success, failure) {
  //   exec(success, failure, 'AndroidSensorManager', 'getDin', []);
  // }

  DeviceSensor.prototype.close = function(success, failure) {
    if (intervalId){
      clearInterval(intervalId);
    }
    exec(success, failure, 'AndroidSensorManager', 'close', []);
  };
  
  var deviceSensor = new DeviceSensor();
  module.exports = deviceSensor;
};

DeviceSensorLoader(require, exports, module);

cordova.define("cordova/plugin/DeviceSensor", DeviceSensorLoader);
