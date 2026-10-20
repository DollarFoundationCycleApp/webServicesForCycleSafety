(function () {
    var mapElement = document.getElementById('map');
    var status = document.getElementById('map-status');
    var styleUrl = mapElement.dataset.styleUrl;
    var center = [Number(mapElement.dataset.centerLon), Number(mapElement.dataset.centerLat)];
    var zoom = Number(mapElement.dataset.zoom);

    // Dev-only fallback when no self-hosted style is configured
    function devStyle() {
        return {
            version: 8,
            sources: {
                osm: {
                    type: 'raster',
                    tiles: ['https://tile.openstreetmap.org/{z}/{x}/{y}.png'],
                    tileSize: 256,
                    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                }
            },
            layers: [{
                id: 'osm',
                type: 'raster',
                source: 'osm',
                paint: {
                    'raster-saturation': -0.85,
                    'raster-contrast': -0.15
                }
            }]
        };
    }

    function showStatus(text) {
        status.textContent = text;
        status.hidden = text === '';
    }

    if (typeof maplibregl === 'undefined') {
        showStatus('Map library failed to load.');
        return;
    }

    var map = new maplibregl.Map({
        container: mapElement,
        style: styleUrl || devStyle(),
        center: center,
        zoom: zoom,
        attributionControl: false
    });
    map.addControl(new maplibregl.NavigationControl(), 'top-right');
    map.addControl(new maplibregl.AttributionControl({
        compact: false,
        customAttribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }));

    // Pin each incident at its [longitude, latitude]. call marker.addTo(map) to show them.
    var incidentMarkers = incidentLocations.map(function (location) {
        return new maplibregl.Marker().setLngLat(location);
    });

    function incidentHeatmap() {
        map.addSource('incidents', {
            type: 'geojson',
            data: {
                type: 'FeatureCollection',
                features: incidentLocations.map(function (location) {
                    return {
                        type: 'Feature',
                        geometry: { type: 'Point', coordinates: location },
                        properties: {}
                    };
                })
            }
        });

        // Each incident raises the heat at its spot; overlapping incidents stack.
        // A cluster of many incidents shows up as a larger red area rather than just a deeper color.
        map.addLayer({
            id: 'incidents-heat',
            type: 'heatmap',
            source: 'incidents',
            paint: {
                'heatmap-weight': 1,

                // Keep isolated incidents from becoming red too easily.
                'heatmap-intensity': 0.6,

                // Blend nearby incidents into smoother regions.
                'heatmap-radius': [
                    'interpolate', ['linear'], ['zoom'],
                    8, 20,
                    11, 40,
                    15, 75,
                    18, 100
                ],

                'heatmap-opacity': 0.75,

                // Transparent -> green -> yellow -> orange -> red.
                'heatmap-color': [
                    'interpolate', ['linear'], ['heatmap-density'],
                    0,    'rgba(46, 160, 67, 0)',
                    0.08, 'rgba(46, 160, 67, 0.35)',
                    0.25, '#66bd63',
                    0.50, '#fee08b',
                    0.75, '#f46d43',
                    1,    '#d73027'
                ]
            }
        });
    }

    map.on('error', function (event) {
        if (!map.isStyleLoaded()) {
            showStatus('Map style could not be loaded from ' + (styleUrl || 'the development tile source') + '.');
        }
        console.error(event.error);
    });

    // Switch the dev basemap between muted (pressed) and its regular colors
    var colorToggle = document.getElementById('toggle-map-color');
    function setMuted(muted) {
        if (muted) {
            map.setPaintProperty('osm', 'raster-saturation', -0.85);
            map.setPaintProperty('osm', 'raster-contrast', -0.15);
            colorToggle.textContent = 'Show regular map';
        } else {
            map.setPaintProperty('osm', 'raster-saturation', 0);
            map.setPaintProperty('osm', 'raster-contrast', 0);
            colorToggle.textContent = 'Show muted map';
        }
        colorToggle.setAttribute('aria-pressed', String(muted));
    }
    colorToggle.addEventListener('click', function () {
        if (!map.isStyleLoaded() || !map.getLayer('osm')) {
            return;
        }
        setMuted(colorToggle.getAttribute('aria-pressed') !== 'true');
    });

    map.on('load', function () {
        incidentHeatmap();
        // Only the dev basemap has the 'osm' layer to recolor
        colorToggle.hidden = !map.getLayer('osm');
        if (styleUrl) {
            showStatus('');
        } else {
            showStatus('Development tiles');
        }
    });
})();
