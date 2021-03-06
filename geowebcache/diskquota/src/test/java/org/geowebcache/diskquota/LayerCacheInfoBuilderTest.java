package org.geowebcache.diskquota;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.geowebcache.grid.GridSet;
import org.geowebcache.grid.GridSetBroker;
import org.geowebcache.grid.GridSubset;
import org.geowebcache.grid.GridSubsetFactory;
import org.geowebcache.layer.TileLayer;
import org.geowebcache.mime.MimeException;
import org.geowebcache.mime.MimeType;
import org.geowebcache.storage.blobstore.file.FilePathGenerator;
import org.geowebcache.util.FileUtils;

public class LayerCacheInfoBuilderTest extends TestCase {

    private LayerCacheInfoBuilder infoBuilder;

    private final int blockSize = 2048;

    private File rootCacheDir;

    private ExecutorService threadPool;

    @Override
    protected void setUp() throws Exception {
        File target = new File("target");
        if (!target.exists() || !target.isDirectory() || !target.canWrite()) {
            throw new IllegalStateException("Can't set up tests, " + target.getAbsolutePath()
                    + " is not a writable directory");
        }

        rootCacheDir = new File(target, getClass().getSimpleName());
        FileUtils.rmFileCacheDir(rootCacheDir, null);
        rootCacheDir.mkdirs();

        threadPool = Executors.newSingleThreadExecutor();

        infoBuilder = new LayerCacheInfoBuilder(rootCacheDir, threadPool, blockSize);
    }

    @Override
    protected void tearDown() throws Exception {
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
        if (rootCacheDir != null) {
            FileUtils.rmFileCacheDir(rootCacheDir, null);
        }
    }

    public void testBuildCacheInfo() throws MimeException, IOException, InterruptedException {

        final String layerName = "MockLayer";
        TileLayer mockLayer = EasyMock.createMock(TileLayer.class);
        EasyMock.expect(mockLayer.getName()).andReturn(layerName).anyTimes();
        GridSet gridSet = new GridSetBroker(false, false).WORLD_EPSG4326;
        GridSubset gridSubset = GridSubsetFactory.createGridSubSet(gridSet);
        EasyMock.expect(mockLayer.getGridSubsets()).andReturn(
                new Hashtable<String, GridSubset>(Collections.singletonMap(gridSubset.getName(),
                        gridSubset))).anyTimes();
        EasyMock.replay(mockLayer);

        final String gridSetId = gridSubset.getName();
        final LayerQuota layerQuota = new LayerQuota(layerName, "MockPolicy");
        final int numFiles = 10;
        final int fileSize = this.blockSize + 1;

        LayerQuotaExpirationPolicy mockPolicy = EasyMock
                .createMock(LayerQuotaExpirationPolicy.class);

        mockPolicy.createInfoFor(EasyMock.eq(layerQuota), EasyMock.eq(gridSetId), EasyMock
                .anyLong(), EasyMock.anyLong(), EasyMock.anyInt());
        EasyMock.expectLastCall().times(numFiles);
        EasyMock.replay(mockPolicy);

        mockSeed(mockLayer, numFiles, fileSize);

        layerQuota.setExpirationPolicy(mockPolicy);

        infoBuilder.buildCacheInfo(mockLayer, layerQuota);

        // be careful and don't wait more than 30s
        long startTime = System.currentTimeMillis();
        long ellapsedTime = 0;
        while (infoBuilder.isRunning(layerName)) {
            Thread.sleep(500);
            ellapsedTime = System.currentTimeMillis() - startTime;
            if (ellapsedTime > 30000) {
                fail(LayerCacheInfoBuilder.class.getSimpleName()
                        + ".buildCacheInfo was running for too long, aborting test!");
            }
        }
        EasyMock.verify(mockLayer);
        EasyMock.verify(mockPolicy);

        // was layer used quota updated?
        final int blockFileSize = (int) Math.ceil((double) fileSize / this.blockSize)
                * this.blockSize;
        final long expectedCacheSize = numFiles * blockFileSize;

        Quota expectedUsedQuota = new Quota(expectedCacheSize, StorageUnit.B);

        Quota usedQuota = layerQuota.getUsedQuota();

        assertTrue(usedQuota.getValue().longValue() > 0);

        assertEquals(0L, usedQuota.difference(expectedUsedQuota).getValue().longValue());
    }

    /**
     * Seeds {@code numFiles} fake tiles of {@code fileSize} each at random tile indices
     * 
     * @param layer
     * @param numFiles
     * @throws MimeException
     * @throws IOException
     */
    private void mockSeed(TileLayer layer, int numFiles, int fileSize) throws MimeException,
            IOException {
        final String layerName = layer.getName();

        final GridSubset gridSubset = layer.getGridSubsets().values().iterator().next();
        final String gridSetId = gridSubset.getName();
        final String prefix = this.rootCacheDir.getAbsolutePath();
        final MimeType mimeType = MimeType.createFromFormat("image/png");
        final long parameters_id = -1;

        final byte[] mockTileContents = new byte[fileSize];
        Arrays.fill(mockTileContents, (byte) 0xFF);

        // just to control the same tile is not created more than once
        Set<String> addedTiles = new HashSet<String>();

        long[] tileIndex;
        while (addedTiles.size() < numFiles) {
            int level = (int) (gridSubset.getZoomStart() + ((gridSubset.getZoomStop() - gridSubset
                    .getZoomStart()) * Math.random()));

            String tileKey = null;
            String[] tilePath;
            do {
                long[] coverage = gridSubset.getCoverage(level);// {minx,miny,maxx,maxy,z}
                long x = (long) (coverage[0] + ((coverage[2] - coverage[0]) * Math.random()));
                long y = (long) (coverage[1] + ((coverage[3] - coverage[1]) * Math.random()));
                tileIndex = new long[] { x, y, level };
                tilePath = FilePathGenerator.tilePath(prefix, layerName, tileIndex, gridSetId,
                        mimeType, parameters_id);
                tileKey = tilePath[0] + File.separator + tilePath[1];
            } while (addedTiles.contains(tileKey));
            addedTiles.add(tileKey);

            File tileDir = new File(tilePath[0]);
            tileDir.mkdirs();
            File tileFile = new File(tileDir, tilePath[1]);
            FileOutputStream fout = new FileOutputStream(tileFile);
            try {
                fout.write(mockTileContents);
            } finally {
                fout.close();
            }
        }

    }
}
