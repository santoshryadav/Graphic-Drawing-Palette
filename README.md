# Paint Board

This project is to show the user a drawing board on the web page.

## Overview

The user will be shown a drawing board on the web page at address localhost:3449.
Once the pallete is visible to user he can draw figures like rectangle, lines, circles and can also
undo the operations.

## Setup

To get an interactive development environment run:  
    First open the command window  
    Then browse in to this project directory  
    Then type lein figwheel
    This will start the processing....  

and now simultaneously open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

##Description and Operating guide:  
1.) You will see a screen similar to windows Paint  
2.) There will be buttons to the right and a drawing area to the left
3.) You can draw lines, rectangles and circles by clciking on each buttons
4.) You can also undo your operations
5.) The clear feature will erase everything on the board
6.) You will be constantly shown the board co-ordinates so that you know where your mouse is
7.) Also you will be shown message as to in which mode you are currently operating
8.) Njoy the paint :)

## License

Copyright © 2014 Santosh R Yadav

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
