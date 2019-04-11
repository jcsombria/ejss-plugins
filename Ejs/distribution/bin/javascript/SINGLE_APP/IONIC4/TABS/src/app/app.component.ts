import { Component } from '@angular/core';

import { Platform } from '@ionic/angular';
import { SplashScreen } from '@ionic-native/splash-screen/ngx';
import { StatusBar } from '@ionic-native/status-bar/ngx';
import { Keyboard } from '@ionic-native/keyboard/ngx';
import { ScreenOrientation } from '@ionic-native/screen-orientation/ngx';

import { Router } from '@angular/router';

declare var app_locking: any;
declare var app_toc: any;
declare var app_simulation_first: any;
declare var app_simulation_index: any;

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  providers: [Keyboard,ScreenOrientation]
})

export class AppComponent {
  tabs: any;
  pages: any;  
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
	  // keyboard.disableScroll(true);
				
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
	this.tabs = [];
	for(var i=0; i<app_toc.length; i++) {
		if(app_toc[i].type == "other_page") {
			this.tabs.push({title: "About", icon: "information-circle"});
		} else if(i == app_simulation_index) {
			this.tabs.push({title: "Simulation", icon: "play"});
		} else {
			this.tabs.push({title: String(app_toc[i].title), icon: "document"});			
		}
	}

	this.pages = app_toc;
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

