import { Component } from '@angular/core';
import { NavController } from 'ionic-angular';

@Component({
  selector: 'page-home',
  templateUrl: 'home.html'
})
export class HomePage {

  constructor(public navCtrl: NavController) {

  }
  stop() {
    // this native call doesn’t have any arguments, just send an action to the native part to 
    // stop listening to sensor data
    window['cordova']['plugins']['sensorManager']['stop']();
  }

}
