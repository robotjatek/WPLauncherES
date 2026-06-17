# WPLauncher ES

A Windows Phone inspired launcher for Android written in OpenGL ES

## Planned features

- WP7/8 style live tiles
- App list
- Pin apps to the screen
- Installed apps and custom "widgets"
- Resizable tiles
- 4/6 column mode

## Tech stack
- Java 
- OpenGL ES3.2
- API Level 34

<img src="img/s2.png" alt="Description" width="250">
<img src="img/s1.png" alt="Description" width="500">

## Milestones

This is a living roadmap, all future milestones are refined as I go, and subject to change.

### M1 - POC / pre-alpha

M1 is all about laying the groundwork. Don't expect anything fancy here. At this phase the application is barely usable.

- [x] 4 columns
- [x] Show *something* on the tiles
- [x] Show app icon on tiles
- [x] Listview for the installed apps
- [x] Icon on list elements
- [x] Swipe between start page and application list page
- [x] Handle tap event on tiles
- [x] Long press on tiles
- [x] Long press on list items
- [x] Context menu
- [x] Pin installed apps to the screen
- [x] Unpin installed apps from the main screen
- [x] Launch app from the app list
- [x] Launch pinned apps
- [x] Uninstall app
- [x] Data structure to store tiles on the main screen
- [x] Rearrange tiles on the grid
- [x] Persist tile arrangement

### M1.5

Consolidation

- [x] Extract draw contexts
- [x] App-list state machine (also fixed fling to keep momentum)
- [x] Tile-grid state machine
- [x] Fix de-select bug when rearranging tiles

### M2 - MVP / alpha

M2 is the beginning of making the launcher usable in everyday use-cases.

- [x] Start working on "launcher apps/widgets/live tiles"
    - [x] Clock
    - [x] Launcher settings
    - [x] Show notification count on tiles
    - [x] Show notification content on corresponding tiles
- [x] Change accent color
- [x] Settings service
- [x] Persist settings
- [x] Resize tiles
- [x] Generic list view
- [x] Stack view
- [x] Basic flex layout
    - [x] Support row and column layout modes
    - [x] Support alignment
    - [x] Flex layout as a child of flex layout
- [x] Absolute layout
  - [x] Use Absolute layout for static tiles
- [x] Use an Icon UI element instead of basic textures for icons to be able to use them in layouts 
- [x] Basic UI elements
  - [x] Button
  - [x] Checkbox

### M2.5

I'm already daily-driving, you shouldn't be.
    
- [x] Checkbox border
- [x] Fix: pin the same tile multiple times
- [x] Don't show uninstall in the context menu for internal and system apps
- [x] Fix: tile overlap after resize
- [x] Remove uninstalled app from the applist/tilegrid
- [x] Fix: Updated app is removed from the grid and app-list instead of updating
- [x] Adjust notification count on tiles - don't allow overlap with the icon
- [x] Fix: Scrolling while swiping on the main screen (scrolling on tile-grid keeps working, WHILE in SWIPING state) -- keeping an eye out, this was a sneaky one
- [x] Fix: Some notifications are counted multiple times
- [x] Use AbsoluteLayout in the ClockHUB tile
- [x] Press "Home" button to jump back to the top of the tile-grid  

### M3 - pre-beta

Try to make it usable for the less crazy people

- [x] GLES 3.2
- [x] Make ContextMenu and MenuOption to use layouts and labels
- [x] Make ListItem use high level UI components
- [x] Embeddable ListView component
  - [x] Make ListPage use ListView internally
- [x] Animations 
  - [x] Swipe animation on StartScreen
  - [x] Animated snap on StartScreen when back or home button is pressed
  - [x] Animate scroll to top on tile-grid when home or back button is pressed
  - [x] Feedback to the user on tap (Rotation/animations/etc)
    - [x] Button
    - [x] Tiles
    - [x] List items
    - [x] InputBox
    - [x] Labels with onclick handlers
    - [x] Context menu
- [x] Gesture system refactor
- [x] Optimizations
  - [x] Fix: janky scroll on the TileGrid + remain in ScrollState while flinging
  - [x] Reduce GC pressure when measuring labels and textblocks during rendering
  - [x] Reduce icon texture size in the app-list to 96x96
  - [x] Reduce icon texture size on the tiles to 256x256
  - [x] On-demand alloc of internal apps and screens
  - [x] Optimize text wrapping in textblock
  - [x] Do not render list items that are outside the viewport
  - [x] Do not store textures when a single uniform color can be used (e.g. for the tile background)
- [x] Bug fixes
  - [x] View crash logs
  - [x] Fix: crash on empty notification content
  - [x] Fix: crash on concurrent access to navigation stack
  - [x] Fix: crash when onDestroy() fires before onSurfaceCreated()
  - [x] Fix: null reference error crash in context menu
  - [x] Fix: call layout() in StackLayout on resize
  - [x] Fix: textblock: force-wrap overly wide words
  - [x] Fix: do not go into context menu state when long-pressing a non-interactive element (fixes null reference crash in context menu)
  - [x] Recycle unused temp bitmaps after texture upload
  - [x] Fix: crash: concurrent modification exception in TextReaderScreen.draw() -> StackLayout.draw() [hopefully fixed by moving gesture handling to the gl thread]
  - [x] Fix: Make context menu aware of the bottom and top margins of the list
  - [x] Fix: Pressing back on the AppList while context menu is opened makes the StartScreen stuck on the TileGrid
- [x] Disable landscape mode
- [x] Search bar in the app-list
  - [x] Kill ListPage in the app-list and replace it with a StackLayout + search box + ListView
  - [x] InputBox component
  - [x] ListView filter
- [x] Scissor test on tiles to prevent drawing out of the tile

### M3.5

Try it at your own risk

- [x] Make the crash-log viewer scrollable (ScrollView component)
- [x] Adjust button border drawing when no icon is set
- [x] About page in Launcher Settings
- [x] Clear search-box when pressing back on the app-list
- [x] Support cursor in input boxes
  - [x] Show current cursor position
  - [x] Set cursor position
  - [x] Insert text at cursor position
  - [x] Delete text at cursor position
- [x] Try perspective projection instead of orthographic
- [x] Use stencil buffer instead of scissor test for drawing tiles (2 pass rendering)
- [x] Use stencil buffer to clip list view
- [x] Use stencil buffer to clip scroll view
- [x] Fix: Uninstalling applications with multiple intents: Only one intent is removed from the applist
- [x] Viewport culling on tile-grid
- [x] Extract ScreenNavigator into a separate component

### M4 - Beta

Feature-creep!

- [ ] More tiles & specific tiles for common apps
  - [ ] Tasks
  - [ ] Photos
  - [ ] Weather
  - [ ] Messenger
  - [ ] Messages
  - [ ] Contacts
- [ ] Generic pivot view (tabbed view)
  - [ ] Settings subpages use pivot view with one tab
- [ ] Multi lang support
- [ ] Light mode/Dark mode support
- [ ] Dropdown
- [ ] Radiobutton
- [ ] Animated tile resize
- [ ] Animate internal app/subpage opening (clock hub, launcher settings)
- [ ] Group apps by the first letter in the app list
- [ ] Set background image for the start screen + transparency option for tiles
- [ ] Re-ask for permissions from the launcher settings
- [ ] Device resolution independent UI
- [ ] Cursor handle in the input box
- [ ] Pin some default tiles to the start screen on first start
- [x] Modal
- [ ] Debug menu
  - [x] Crash app button
  - [ ] Reset StartPage state machine button

### M5 - RC

- [ ] Landscape mode support
- [ ] Background image with transparent tiles
- [ ] 6 column mode setting
- [ ] WP7/7.8 start screen setting
- [ ] Custom tile color
- [ ] Application icon
- [ ] Privacy policy
- [ ] License

### Bugs that need repro:

- [ ] Fix: black bar in the place of the notification count when all previous notifications were dismissed (on real hardware, needs repro)
- [ ] Fix: sometimes tiles don't spin when there is a notification (detected on real hardware, needs repro)
- [ ] Fix: Tile bg after resize is dark (needs repro)

### Backlog

Nice to have, but I feel no pressure to implement them for now 

- [ ] Layout invalidation when child size changes (call layout() in the parent layout on a component resize)
- [ ] Reconsider tile reflow logic (tile occupancy bool map, for every tile top to bottom -> remove -> find the highest available pos where it fits -> place?)

### Current progress

M4 is in progress

## Planned live custom tiles / "widgets"

Widgets with live data

- Me (Timeline / notifications / tasks / slideshows / weather info / travel info / clock / twitter / fb / etc.)
- Photos slideshow
- Messages
- Weather
- Clock / Time
- News feed
- Tasks
- Calendar
- Email
- Hungarian name days

*Information gathering via public API-s. When public API-s are not available extract information from system notifications if its possible*
