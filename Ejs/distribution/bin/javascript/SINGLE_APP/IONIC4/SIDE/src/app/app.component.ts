import { Component, ViewChild } from '@angular/core';
import { Platform } from '@ionic/angular';
import { StatusBar } from '@ionic-native/status-bar/ngx';
import { SplashScreen } from '@ionic-native/splash-screen/ngx';
import { Keyboard } from '@ionic-native/keyboard/ngx';
import { ScreenOrientation } from '@ionic-native/screen-orientation/ngx';

import { Router } from '@angular/router';

declare var app_locking: any;
declare var app_toc: any;
declare var app_menu_title: any;
declare var app_simulation_first: any;
declare var app_simulation_index: any;

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  providers: [Keyboard,ScreenOrientation]
})

export class AppComponent {
  pages: any;
  app_menu_title: string;
  app_simulation_first: boolean;
  app_simulation_index: number;

  constructor(
	private router: Router, 
	platform: Platform, 
	statusBar: StatusBar, 
	splashScreen: SplashScreen, 
	keyboard: Keyboard, 
	screenOrientation: ScreenOrientation) {
    
	platform.ready().then(() => {
      // statusBar.backgroundColorByHexString('#ffffff');
	  statusBar.styleDefault();
      splashScreen.hide();
	  keyboard.hideFormAccessoryBar(false);
				
	  // locking screen orientation
	  switch(app_locking) { 
		case 0: // portrait
			screenOrientation.lock("portrait").catch(function() {});
			break;
		case 1: // landscape
			screenOrientation.lock("landscape").catch(function() {});
			break;
		default: // nothing
	  }
    });	

	// app vars
	this.pages = app_toc;
	this.app_menu_title = app_menu_title;
	this.app_simulation_first = app_simulation_first;
	this.app_simulation_index = app_simulation_index;	
  }

  ngOnInit() {
	if(this.app_simulation_first)
		this.router.navigate(['/home/' + app_simulation_index]);		
	else
		this.router.navigate(['/home/0']);		
  }
   
  selectPage(index) {
	let page = this.pages[index]
	if(page.type == "other_page")
		return "/about";		
	else
		return "/home/" + index;		
  }
}
