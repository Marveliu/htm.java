package org.numenta.nupic.network;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.numenta.nupic.ValueList;
import org.numenta.nupic.network.Network.Node;

/**
 * Default implementation of a {@link Sensor} for inputting data from
 * a file.
 * 
 * All {@link Sensor}s represent the bottom-most level of any given {@link Network}. 
 * Sensors are used to connect to a data source and feed data into the Network, therefore
 * there are no nodes beneath them or which precede them within the Network hierarchy, in
 * terms of data flow. In fact, a Sensor will throw an {@link Exception} if an attempt to 
 * connect another {@link Node} to the input of a Node containing a Sensor is made.
 *  
 * @author David Ray
 */
public class FileSensor implements Sensor<File> {
    private static final int HEADER_SIZE = 3;
    private static final int BATCH_SIZE = 20;
    private static final boolean DEFAULT_PARALLEL_MODE = false;
    
    private BatchedCsvStream<String[]> stream;
    private SensorParams params;
    
    public FileSensor(SensorParams params) {
        this.params = params;
        
        if(!params.hasKey("PATH")) {
            throw new IllegalArgumentException("Passed improperly formed Tuple: no key for \"PATH\"");
        }
        
        File f = new File((String)params.get("PATH"));
        if(!f.exists()) {
            throw new IllegalArgumentException("Passed improperly formed Tuple: invalid PATH: " + params.get("PATH"));
        }
        
        try {
            Stream<String> stream = Files.lines(f.toPath(), Charset.forName("UTF-8"));
            this.stream = BatchedCsvStream.batch(
                stream, BATCH_SIZE, DEFAULT_PARALLEL_MODE, HEADER_SIZE);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Sensor<File> create(SensorParams p) {
        Sensor<File> fs = new FileSensor(p);
        return fs;
    }
    
    @Override
    public SensorParams getParams() {
        return params;
    }
    
    /**
     * Returns the configured {@link MetaStream} if this is of
     * Type Stream, otherwise it throws an {@link UnsupportedOperationException}
     * 
     * @return  the MetaStream
     */
    @SuppressWarnings("unchecked")
    @Override
    public <K> MetaStream<K> getInputStream() {
        return (MetaStream<K>)stream;
    }
    
    /**
     * Returns the values specifying meta information about the 
     * underlying stream.
     */
    public ValueList getMeta() {
        return stream.getMeta();
    }

}
