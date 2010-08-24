package au.gov.ga.worldwind.layers.tiled.image.delegate;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public interface ImageReaderDelegate
{
	BufferedImage readImage(URL url) throws IOException;
}