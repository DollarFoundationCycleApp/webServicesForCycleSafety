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
            layers: [{ id: 'osm', type: 'raster', source: 'osm' }]
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
        style: styleUrl ? styleUrl : devStyle(),
        center: center,
        zoom: zoom,
        attributionControl: false
    });
    map.addControl(new maplibregl.NavigationControl(), 'top-right');
    map.addControl(new maplibregl.AttributionControl({
        compact: false,
        customAttribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }));

    map.on('error', function (event) {
        if (!map.isStyleLoaded()) {
            showStatus('Map style could not be loaded from ' + (styleUrl || 'the development tile source') + '.');
        }
        console.error(event.error);
    });

    map.on('load', function () {
        showStatus(styleUrl ? '' : 'Development tiles');
    });
})();
