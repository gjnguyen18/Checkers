package application;

import javafx.scene.Group;
import javafx.scene.Node;

/**
 * This abstract class represents a screen. Multiple instances of classes which inherit screen
 * may exist at the same time, but only one would be showing at a time.
 * 
 * @author Gia-Phong Nguyen
 *
 */
public class Screen {

//	private ArrayList<Node> elements;
	private Group group;
	private Group root;

	public Screen(Group root) {
		group = new Group();
		this.root = root;
		root.getChildren().add(group);
	}

	/**
	 * Show all elements of screen
	 */
	public void show() {
		group.setVisible(true);
	}

	/**
	 * Hide all elements of screen
	 */
	public void hide() {
		group.setVisible(false);
	}
	
	protected Group getRoot() {
		return root;
	}

	public void addElement(Node n) {
		group.getChildren().add(n);
	}

	public void removeElement(Node n) {
		group.getChildren().remove(n);
	}
	
	public void deleteScreen() {
		root.getChildren().remove(group);
		group = null;
	}
}
