[**Foreword**](#Foreword)  
[**What is EJS?**](#WhatisEJS)  
[**Download**](#Download)  
[**Credits**](#Credits)  
[**Links**](#Links)  

<a name="Foreword"></a>
# Foreword

Simulations play an increasingly important role in the way we teach or do Science. This is especially true in Education, where computers are being used more and more as a way to make lectures more attractive to students, and to help them achieve a deeper understanding of the subject being taught.

However, it cannot be said that computer simulations are used by most of our teachers and educators. In many cases, this is due to the fact that teachers are reluctant to use a technology they do not fully understand or control. In other cases, it may be that they have not found a product that completely meets their educational needs.

A good solution to both of those points is to help teachers create their own simulations. We have found that, by creating a simulation, many teachers get a new perspective on the phenomenon they are trying to explain, which almost always increases their enthusiasm about the use of this technology for their students.

An alternative approach, which is also very promising, is to let students create their own simulations, thus engaging in what educational researchers call constructive modeling, Constructionism (learning theory) or simply 'learning-by-making'. This approach has the advantage of getting the student to do science in an exploratory and constructive way, which achieves many of the recommended best-practices in the classroom.

It is true that creating a simulation by oneself requires extra effort. The starting point, and this is the important part, is a full understanding of the phenomenon being simulated. From this, some technicalities are certainly needed in order to express the behavior of the phenomenon in a computer simulation.

Easy Java/JavaScript Simulations was written to address this problem. EJS has been specifically designed to teach a broad audience how to create scientific simulations in Java or JavaScript, in a quick and simple way.

The target audience for EJS is science students, teachers or researchers who have a basic knowledge of programming computers, but who cannot afford the big investment of time needed to create a complete graphical simulation. They are able to describe the models of phenomena of their respective disciplines in terms of equations or algorithms. But they still need an extra effort to create a sophisticated, interactive graphical user interface, in the style of simulations and software programs one can find nowadays in the Internet.

With this situation in mind, EJS has been designed to help a person who wants to create a simulation to concentrate most of his/her time in writing and refining the algorithms of the underlying scientific model (which is his/her real expertise), and to dedicate the minimum possible amount of time to the programming techniques. And yet obtain an independent, high performance, Internet-aware, final product.

The choice of Java and JavaScript as development languages is justified in terms of their wide acceptance by the international Internet community, and the fact that they are supported under virtually any software platform. This means that EJS, and the simulations created using it, can be used in most, if not all, operating systems. The simulations can also be distributed via the Internet and run within web pages. JavaScript simulations run in any standard Web browser without any special requirement. Java simulations will require a Java-enabled web browser.

<a name="WhatisEJS"></a>
# What is EJS?

Easy Java/JavaScript Simulations is a software tool (of the type known as code generators) designed for the creation of discrete computer simulations.

A discrete computer simulation, or simply a computer simulation, is a computer program that tries to reproduce, for pedagogical or scientific purposes, a natural phenomenon through the visualization of the different states that it can have. Each of these states is described by a set of variables that change in time due to the iteration of a given algorithm.

All this means that EJS is a program that helps you create other programs; more precisely, to create scientific simulations.

There exist many programs that help create other programs. What makes EJS different from most other products is that EJS is not designed to make life easier for professional programmers, but has been conceived by science teachers, for science teachers and students. That is, for people who are more interested in the content of the simulation, the simulated phenomenon itself, and much less in the technical aspects needed to build the simulation.

Easy Java/JavaScript Simulations is a modeling and authoring tool expressly devoted to this task. It has been designed to let its user work at a high conceptual level, using a set of simplified tools, and concentrating most of his/her time on the scientific aspects of our simulation, asking the computer to automatically perform all the other necessary but easily automated tasks.

Nevertheless, the final result, which is automatically generated by EJS from your description, can, in terms of efficiency and sophistication, be taken as the creation of a professional programmer.

In particular, EJS creates Java and JavaScript applications that are platform independent, or simulations that can be visualized using any Web browser (and therefore distributed through the Internet), which read data across the net, and which can be controlled using scripts from within web pages. (JavaScript simulations run in any standard web browser without further modification. Java applets require a Java plug-in installed in your browser.)

Because there is an educational value in the process of creating a simulation, EJS can also be used as a pedagogical tool itself. With it, teachers can ask their students to create a simulation by themselves, perhaps by following some guidelines which provided by the instructor. Used in this way, EJS can help students make their conceptualizations explicit. Used in groups, through [social constructionism](http://en.wikipedia.org/wiki/Social_constructionism), it can help improve the students' abilities to discuss and communicate about science, learning to be scientist instead of learning about it.

<a name="Download"></a>
# Download

## Stable version

EjsS stable version is available at [the release folder](https://gitlab.com/ejsS/tool/tree/master/Release).

Note that the file name ending _yymmdd indicates year, month, day of built.

IMPORTANT: EjsS 5.X requires Java Runtime Environment (JRE) 1.7 or 1.8. Is is NOT fully Java 9 compatible.

## Beta version

You can also download beta releases. They allow you to see the latest features.

## Running EjsS

* Unzip the distribution file. This will create a new folder, e.g. EJS_5.3.
* Start EJS with the file EjsConsole.jar.

<a name="Credits"></a>
# Credits

Easy Java/Javascript Simulations is the exclusive copyright of its author, [Francisco Esquembre](http://www.um.es/fem/PersonalWiki), who distributes it under a [GNU GPL license](http://www.gnu.org/copyleft/gpl.html).

The Javascript library that supports the Javascript flavour of EJS is copyright of Francisco Esquembre and [Félix Jesús García Clemente](http://webs.um.es/fgarcia/miwiki/doku.php?id=home).

However, Easy Java/Javascript Simulations, in its current version, is the result of a project that has been carried out for several years and under different conceptions and implementations. For this reason, it owes a lot to contributions from several groups of people.

Easy Java/Javascript Simulations is part of the [Open Source Physics](http://www.opensourcephysics.org/) project, created by Wolfgang Christian, Davidson College, North Carolina, USA.

Here is a list of contributors in alphabetical order:

* Doug Brown, Cabrillo College, California, USA.
* Wolfgang Christian, Davidson College, North Carolina, USA.
* Fu-Kwun Hwang, National Taiwan Normal University, Taiwan.
* Howard Kistler, [Hexidec](http://www.hexidec.com/) (for eKit, the HTML editor used for the Description pages).
* Pat Niemeyer (pat@pat.net) and other developers of [BeanShell](http://www.beanshell.org/), the parser used for syntax checking.
* José Sánchez, Gonzalo Farias, Héctor Vargas, Luis de la Torre and Jesús Chacón, National University for Distance Education, Spain.
* Frank F. Schweickert, AMSTEL Institute, University of Amsterdam, The Nederlands (for his contributions to setting up this Wiki).
* Yuri B. Senichenkov and Andrei Goussev, Saint Petersburg Polytechnic University, Russia (for the DoPri and Radau ODE solvers).
* [Jeevanandam Madanagopal](http://www.myjeeva.com/) for com.myjeeva.image.ImageManipulation.java.

The different translations are due to:

* English and Spanish: Francisco Esquembre, Universidad de Murcia, Spain.
* Catalan: Paco Rivière, [http://pacoriviere.cat/](http://pacoriviere.cat), Freelance Consultant.

Icons are taken from different sources. I hope to be able to give credits to all those who deserve it, including:

* Hylke Bon, for icons under the Creative Commons license.
* [Freepik](http://www.freepik.com/) from [Flaticon](http://www.flaticon.com/). Licensed by [Creative Commons BY 3.0](http://creativecommons.org/licenses/by/3.0).
* [Yannick](http://www.flaticon.com/authors/yannick) from [Flaticon](http://www.flaticon.com/). Licensed by [Creative Commons BY 3.0](http://creativecommons.org/licenses/by/3.0).
* [Madebyoliver](http://www.flaticon.com/authors/madebyoliver) from [Flaticon](http://www.flaticon.com/). Licensed by [Creative Commons BY 3.0](http://creativecommons.org/licenses/by/3.0).

The development of EJS and of this Wiki has been financed by several institutions and administrations. Among them:

* The Spanish Ministry of Research, through several research grants (since ever!)
* The Seneca Foundation (Regional Research Agency of the Region of Murcia, Spain), through a series of research grants starting in 2003.
* The [Wilhelm und Else Heraeus Stiftung](http://www.we-heraeus-stiftung.de/index.html) (or Foundation), through a research grant from Sept. 2008 to Sept. 2010.

A special place of honor is, of course, reserved for Paco's family: his wife Araceli and his daughters Araceli and Maria Belén, for all those weekend evenings that he spent (and still spends!) in front of the computer.

<a name="Links"></a>
# Links

## EJS at comPADRE digital library
EjsS is part of the Open Source Physics (OSP) Collection hosted by the comPADRE Digital Library, a network of educational resource collections supporting teachers and students in Physics and Astronomy. As a comPADRE user you may explore collections designed to meet your specific needs and help build the network by adding resources, commenting on resources, and starting or joining discussions. You must register (registration is free) and login with a ComPADRE account to be able to upload files.

* [EJS in the comPADRE digital library](http://www.compadre.org/osp/search/categories.cfm?t=Overview)
* [EjsS Users' Group](http://www.compadre.org/osp/user/Community.cfm)
* [Contribute examples to comPADRE](https://www.um.es/fem/EjsWiki/Main/OSPContribute)

## Webcasts and videos

*  [Webcasts and videos](https://www.um.es/fem/EjsWiki/Main/Webcasts)

## EJS at Facebook sites

* [Singapore OSP on Facebook: come join the community and support education through simulation that is easy to make!](http://www.facebook.com/pages/Easy-Java-Simulation-Official/132622246810575?sk=wall) 

## Other links
The following links contain references about Easy Java Simulations in other web servers, some corresponding to (paper) published work.

* [Paper in 'The Physics Teacher'](http://www.opensourcephysics.org/items/detail.cfm?ID=7284)
* ['Modeling Science' book project](http://www.compadre.org/OSP/filingcabinet/share.cfm?UID=4856&FID=17708&code=873A9F5BC1)
* [EJS Spanish book](http://www.um.es/fem/Ejs/LibroEjs/index.html)
* [Learning and Teaching Mathematics using Simulations](http://www.degruyter.com/cont/fb/ma/detailEn.cfm?id=IS-9783110250053-1&ad=he) (Plus 2000 Examples from Physics). Book by Dieter Röss, De Gruyter Textbook, 2011.
* [Courses on EJS](http://www.euclides.dia.uned.es/simulab-pfp/index_en.htm)
* [A Simple Demonstration for the Static Ladder Problem. Mario Belloni](http://www.compadre.org/osp/features/newsdetail.cfm?id=192). The Physics Teacher, Volume 46, Issue 8, pp. 503-504, November 2008

## Projects that used EJS for their simulations:

* [MOSEM² project](http://www.mosem.eu/). You can download the simulations created by the project from [this link](https://www.um.es/fem/EjsWiki/uploads/Download/Mosem2Simulations.zip). 


