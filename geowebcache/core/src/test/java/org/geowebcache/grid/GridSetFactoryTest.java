package org.geowebcache.grid;

import junit.framework.TestCase;


public class GridSetFactoryTest extends TestCase {

    public void testResolutionsArrayEPSG4326() throws Exception {
        BoundingBox extent = new BoundingBox(-180, -90, 180, 90);
        double[] resolutions = {180.0/256, 180.0/512, 180.0/1024, 180.0/2048};
        
        GridSet gridSet = GridSetFactory.createGridSet("test", SRS.getEPSG4326(), extent, false, resolutions, null, null, 0.00028, null, 256, 256, false);
        
        assertEquals("test", gridSet.name);
        assertEquals(-180.0, gridSet.baseCoords[0]);
        assertEquals(-90.0, gridSet.baseCoords[1]);
        assertEquals(resolutions.length, gridSet.gridLevels.length);
        
        Grid grid0 = gridSet.gridLevels[0];
        
        assertEquals(180.0/256, grid0.resolution);
        assertEquals(2L, grid0.extent[0]);
        assertEquals(1L, grid0.extent[1]);

        Grid grid3 = gridSet.gridLevels[3];
        
        assertEquals(180.0/2048, grid3.resolution);
        assertEquals((long) Math.pow(2, 4), grid3.extent[0]);
        assertEquals((long) Math.pow(2, 3), grid3.extent[1]);
    }

    public void testResolutionsArrayEPSG3785() throws Exception {
        BoundingBox extent = new BoundingBox(-20037508.34,-20037508.34,20037508.34,20037508.34);
        double[] resolutions = {(20037508.34*2)/256, (20037508.34*2)/512, (20037508.34*2)/1024, (20037508.34*2)/2048};
        
        GridSet gridSet = GridSetFactory.createGridSet("test", SRS.getEPSG3857(), extent, false, resolutions, null, null, 0.00028, null, 256, 256, false);
        
        Grid grid0 = gridSet.gridLevels[0];
        
        assertEquals((20037508.34*2)/256, grid0.resolution);
        assertEquals(1L, grid0.extent[0]);
        assertEquals(1L, grid0.extent[1]);
        
        Grid grid3 = gridSet.gridLevels[3];
        
        assertEquals((long) Math.pow(2, 3), grid3.extent[0]);
        assertEquals((long) Math.pow(2, 3), grid3.extent[1]);
    }
    
    public void testResolutionsArrayTricky1() throws Exception {
        // This should be expanded in the X direction
        BoundingBox extent = new BoundingBox(-173, -90, 180, 96);
        double[] resolutions = {180.0/200, 180.0/400, 180.0/800};
        
        GridSet gridSet = GridSetFactory.createGridSet("test", SRS.getEPSG4326(), extent, false, resolutions, null, null, 0.00028, null, 200, 200, false);
        
        assertEquals(-173.0, gridSet.baseCoords[0]);
        assertEquals(-90.0, gridSet.baseCoords[1]);
        assertEquals(resolutions.length, gridSet.gridLevels.length);
        
        Grid grid0 = gridSet.gridLevels[0];
        
        assertEquals(180.0/200, grid0.resolution);
        assertEquals(2L, grid0.extent[0]);
        assertEquals(2L, grid0.extent[1]);

        Grid grid3 = gridSet.gridLevels[2];
        
        assertEquals(180.0/800, grid3.resolution);
        assertEquals(8L, grid3.extent[0]);
        assertEquals(5L, grid3.extent[1]);
        
    }
    
    public void testBoundingBoxEPSG4326() throws Exception {
        BoundingBox extent = new BoundingBox(-180, -90, 180, 90);
        
        GridSet gridSet = GridSetFactory.createGridSet("test", SRS.getEPSG4326(), extent, false, 4, null, 0.00028, 256, 256, false);
        
        assertEquals("test", gridSet.name);
        assertEquals(-180.0, gridSet.baseCoords[0]);
        assertEquals(-90.0, gridSet.baseCoords[1]);
        assertEquals(4, gridSet.gridLevels.length);
        
        Grid grid0 = gridSet.gridLevels[0];
        
        assertEquals(180.0/256, grid0.resolution);
        assertEquals(2L, grid0.extent[0]);
        assertEquals(1L, grid0.extent[1]);

        Grid grid3 = gridSet.gridLevels[3];
        
        assertEquals(180.0/2048, grid3.resolution);
        assertEquals((long) Math.pow(2, 4), grid3.extent[0]);
        assertEquals((long) Math.pow(2, 3), grid3.extent[1]);
    }
    
    public void testBoundingBoxEPSG3785() throws Exception {
        BoundingBox extent = new BoundingBox(-20037508.34,-20037508.34,20037508.34,20037508.34);
        GridSet gridSet = GridSetFactory.createGridSet("test", SRS.getEPSG3857(), extent, false, 6, null, 0.00028, 256, 256, false);
        
        Grid grid0 = gridSet.gridLevels[0];
        
        assertEquals((20037508.34*2)/256, grid0.resolution);
        assertEquals(1L, grid0.extent[0]);
        assertEquals(1L, grid0.extent[1]);
        
        Grid grid3 = gridSet.gridLevels[3];
        
        assertEquals((long) Math.pow(2, 3), grid3.extent[0]);
        assertEquals((long) Math.pow(2, 3), grid3.extent[1]);
    }
    
    public void testBoundingBoxTricky1() throws Exception {
        BoundingBox extent = new BoundingBox(-180, -90, 172, 90);
        
        GridSet gridSet = GridSetFactory.createGridSet("test", SRS.getEPSG4326(), extent, false, 4, null, 0.00028, 256, 256, false);
        
        assertEquals("test", gridSet.name);
        assertEquals(-180.0, gridSet.baseCoords[0]);
        assertEquals(-90.0, gridSet.baseCoords[1]);
        assertEquals(4, gridSet.gridLevels.length);
        
        Grid grid0 = gridSet.gridLevels[0];
        
        assertEquals(180.0/256, grid0.resolution);
        assertEquals(2L, grid0.extent[0]);
        assertEquals(1L, grid0.extent[1]);
    }
    
    public void testBoundingBoxTricky2() throws Exception {
        BoundingBox extent = new BoundingBox(-180, -90, 180, 82);
        
        SRS srs = SRS.getEPSG4326();
        
        GridSet gridSet = GridSetFactory.createGridSet("test", srs, extent, false, 4, null, 0.00028, 256, 256, false);
        
        assertEquals("test", gridSet.name);
        assertEquals(-180.0, gridSet.baseCoords[0]);
        assertEquals(-90.0, gridSet.baseCoords[1]);
        assertEquals(4, gridSet.gridLevels.length);
        
        Grid grid0 = gridSet.gridLevels[0];
        
        assertEquals(180.0/256, grid0.resolution);
        assertEquals(2L, grid0.extent[0]);
        assertEquals(1L, grid0.extent[1]);
    }
}