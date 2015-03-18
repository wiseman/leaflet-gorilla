# leaflet-gorilla

A renderer for [Gorilla REPL](http://gorilla-repl.org/) that generates
[Leaflet maps](http://leafletjs.com/).

![leaflet-gorilla screenshot](/media/screenshots/leaflet-gorilla-screenshot-1.png?raw=true "leaflet-gorilla screenshot")

Still under development.

## Examples

### Prologue

```
(ns gentle-shelter
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [com.lemondronor.leaflet-gorilla :as lg]
   [gorilla-plot.core :as plot]))
```

### Points
```
(def points [[35.059444 -118.151667]
             [34.200556 -118.358611]
             [33.9425, -118.408056]
             [34.5975 -117.383056]])

(lg/leaflet points)

(lg/leaflet [[37.78738 -122.240335]
             [37.788633 -122.237268]
             [37.788525 -122.237366]]])

(lg/leaflet [:points points])
```

### Lines
```
(lg/leaflet [:line points])
```

### Polygons
```
(lg/leaflet [:polygon [[41.00 -109.05]
                       [40.99 -102.06]
                       [36.99 -102.03]
                       [36.99 -109.04]
                       [41.00 -109.05]]])
```

## License

Copyright © 2015 John Wiseman <jjwiseman@gmail.com>

Distributed under the MIT license.
