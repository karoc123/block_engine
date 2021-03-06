package de.oth.blocklib.renderer;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import de.oth.blocklib.Configuration;
import de.oth.blocklib.models.RawModel;
import de.oth.blocklib.textures.Texture;
import de.oth.blocklib.textures.TextureLoader;

/**
 * Loads an object into a VAO.
 * The object needs position data, texture data, normals data and indices.
 * Returns a RawModel after uploading the the data
 */
public class Loader {
	
	private List<Integer> vaos = new ArrayList<Integer>();
	private List<Integer> vbos = new ArrayList<Integer>();
	private List<Integer> textures = new ArrayList<Integer>();
	
	/**
	 * Fills VAO with Data for RawModel
	 * 
	 * @param positions
	 *            array of vertices
	 * @param textureCoords
	 *            array of texture coordinates
	 * @param normals
	 * 			array of normals
	 * @param indices
	 *            array of indices
	 * @return RawModel with all arrays
	 */
	public RawModel loadToVAO(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
		int vaoID = createVAO();
		bindIndicesBuffer(indices);
		storeDataInAttributeList(0, 3, positions);
		storeDataInAttributeList(1, 2, textureCoords);
		storeDataInAttributeList(2, 3, normals);
		ubindVAO();
		return new RawModel(vaoID, indices.length);
	}
	
	/**
	 * Fills VAO with Data
	 * 
	 * @param positions
	 *            array of vertices
	 * @param length
	 * 			  length of the float buffer
	 * @return RawModel with all arrays
	 */
	public RawModel loadToVAO(FloatBuffer positions, int length) {
		int vaoID = createVAO();
		storeDataInAttributeList(0, 3, positions);
		ubindVAO();
		return new RawModel(vaoID, length);
	}	
	
	/**
	 * To load a single Texture from res/filename.png
	 * 
	 * @param fileName
	 *            only the name without .png
	 * @return the id of the texture generated from opengl
	 */
	public int loadTexture(String fileName){
		Texture texture = null;
		// load texture in jar
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName+".png");
		texture = TextureLoader.getTexture("PNG", is);

		// load texture without jar
//		try {
//			texture = TextureLoader.getTexture("PNG", new FileInputStream("res/"+fileName+".png"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		// MIPMAPPING
		if(Configuration.MIPMAPPING){
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST);
		}
		int textureID = texture.getTextureID();
		textures.add(textureID);
		
		return textureID;
	}
	
	/**
	 * Deletes all loaded resources with glDeleteXXX.
	 */
	public void cleanUp(){
		for(int vao:vaos){
			GL30.glDeleteVertexArrays(vao);
		}
		for(int vbo:vbos){
			GL15.glDeleteBuffers(vbo);
		}
		for(int texture:textures){
			GL11.glDeleteTextures(texture);
		}
	}
	
	/**
	 * Creates VAO with opengl
	 * @return returns created vao id
	 */
	private int createVAO(){
		int vaoID = GL30.glGenVertexArrays();
		vaos.add(vaoID);
		GL30.glBindVertexArray(vaoID);
		return vaoID;
	}
	
	/**
	 * stores data in glBufferData
	 * @param attributeNumber
	 * @param coordinateSize
	 * @param data
	 */
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, float[] data){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * stores data in glBufferData
	 * @param attributeNumber
	 * @param coordinateSize
	 * @param data
	 */
	private void storeDataInAttributeList(int attributeNumber, int coordinateSize, FloatBuffer buffer){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
	}
	
	/**
	 * Unbinds VAO
	 */
	private void ubindVAO(){
		GL30.glBindVertexArray(0);
	}
	
	private void bindIndicesBuffer(int[] indices){
		int vboID = GL15.glGenBuffers();
		vbos.add(vboID);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer,  GL15.GL_STATIC_DRAW);
	}
	
	private IntBuffer  storeDataInIntBuffer(int[] data){
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
	
	private FloatBuffer storeDataInFloatBuffer(float[] data){
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
}
