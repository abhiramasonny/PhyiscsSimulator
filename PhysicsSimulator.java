import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PhysicsSimulator extends JFrame {
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private static final double GRAVITY = 9.81;
    private static final double TIME_STEP = 0.16;
    private static final double GROUND_FRICTION = 0.1; // Ground friction coefficient
    private static final int GROUND_HEIGHT = 30; // Ground height

    private List<PhysicsObject> objects;
    private Timer timer;
    private JPanel simulationPanel;
    private Random random;

    private JTextField radiusField, massField, velocityXField, velocityYField, sidesField;
    private JSlider elasticitySlider;
    private JButton addCircleButton, addPolygonButton;

    public PhysicsSimulator() {
        objects = new ArrayList<>();
        random = new Random();

        setTitle("Physics Simulator");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        createSimulationPanel();
        createControlPanel();

        timer = new Timer(16, e -> updateSimulation());
        timer.start();
    }
    private void addCircleAtMouse(int x, int y) {
        try {
            double radius = Double.parseDouble(radiusField.getText());
            double mass = Double.parseDouble(massField.getText());
            double vx = Double.parseDouble(velocityXField.getText());
            double vy = Double.parseDouble(velocityYField.getText());
            double elasticity = elasticitySlider.getValue() / 100.0;
    
            // Ensure the circle is created within the boundaries
            double adjustedX = Math.max(radius, Math.min(x, simulationPanel.getWidth() - radius));
            double adjustedY = Math.max(radius, Math.min(y, simulationPanel.getHeight() - radius - GROUND_HEIGHT));
            Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
    
            objects.add(new Circle(adjustedX, adjustedY, vx, vy, radius, mass, elasticity, color));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }
    
    private void createSimulationPanel() {
        simulationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw the ground
                g.setColor(Color.BLACK);
                g.fillRect(0, getHeight() - GROUND_HEIGHT, getWidth(), GROUND_HEIGHT);

                // Draw objects
                for (PhysicsObject obj : objects) {
                    obj.draw(g);
                }
            }
        };
        simulationPanel.setBackground(Color.WHITE);
        simulationPanel.setPreferredSize(new Dimension(WIDTH - 200, HEIGHT));
        add(simulationPanel, BorderLayout.CENTER);

        simulationPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                addCircleAtMouse(e.getX(), e.getY());
            }
        });
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        radiusField = new JTextField("20", 5);
        massField = new JTextField("1", 5);
        velocityXField = new JTextField("0", 5);
        velocityYField = new JTextField("0", 5);
        sidesField = new JTextField("5", 5); // For polygon shapes
        elasticitySlider = new JSlider(0, 100, 90);
        elasticitySlider.setMajorTickSpacing(25);
        elasticitySlider.setPaintTicks(true);
        elasticitySlider.setPaintLabels(true);

        addCircleButton = new JButton("Add Circle");
        addCircleButton.addActionListener(e -> addCircle());

        addPolygonButton = new JButton("Add Polygon");
        addPolygonButton.addActionListener(e -> addPolygon());

        controlPanel.add(createLabeledComponent("Radius", radiusField));
        controlPanel.add(createLabeledComponent("Mass:", massField));
        controlPanel.add(createLabeledComponent("Velocity X:", velocityXField));
        controlPanel.add(createLabeledComponent("Velocity Y:", velocityYField));
        controlPanel.add(createLabeledComponent("Elasticity:", elasticitySlider));
        controlPanel.add(createLabeledComponent("Sides (for Polygon):", sidesField));
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(addCircleButton);
        controlPanel.add(addPolygonButton);

        add(controlPanel, BorderLayout.EAST);
    }

    private JPanel createLabeledComponent(String label, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(label));
        panel.add(component);
        return panel;
    }

    private void addCircle() {
        try {
            double radius = Double.parseDouble(radiusField.getText());
            double mass = Double.parseDouble(massField.getText());
            double vx = Double.parseDouble(velocityXField.getText());
            double vy = Double.parseDouble(velocityYField.getText());
            double elasticity = elasticitySlider.getValue() / 100.0;

            double x = random.nextDouble() * (simulationPanel.getWidth() - 2 * radius) + radius;
            double y = random.nextDouble() * (simulationPanel.getHeight() - 2 * radius - GROUND_HEIGHT) + radius;
            Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());

            objects.add(new Circle(x, y, vx, vy, radius, mass, elasticity, color));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    private void addPolygon() {
        try {
            double radius = Double.parseDouble(radiusField.getText());
            double mass = Double.parseDouble(massField.getText());
            double vx = Double.parseDouble(velocityXField.getText());
            double vy = Double.parseDouble(velocityYField.getText());
            int sides = Integer.parseInt(sidesField.getText());
            double elasticity = elasticitySlider.getValue() / 100.0;

            double x = random.nextDouble() * (simulationPanel.getWidth() - 2 * radius) + radius;
            double y = random.nextDouble() * (simulationPanel.getHeight() - 2 * radius - GROUND_HEIGHT) + radius;
            Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());

            objects.add(new PolygonObject(x, y, vx, vy, radius, mass, elasticity, sides, color));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    private void updateSimulation() {
        for (PhysicsObject obj : objects) {
            obj.vy += GRAVITY * TIME_STEP;

            obj.x += obj.vx * TIME_STEP;
            obj.y += obj.vy * TIME_STEP;

            applyGroundFriction(obj);

            if (obj.x - obj.radius < 0 || obj.x + obj.radius > simulationPanel.getWidth()) {
                obj.vx *= -obj.elasticity;
                obj.x = Math.max(obj.radius, Math.min(simulationPanel.getWidth() - obj.radius, obj.x));
            }
            if (obj.y - obj.radius < 0 || obj.y + obj.radius > simulationPanel.getHeight() - GROUND_HEIGHT) {
                obj.vy *= -obj.elasticity;
                obj.y = Math.max(obj.radius, Math.min(simulationPanel.getHeight() - obj.radius - GROUND_HEIGHT, obj.y));
            }
        }

        for (int i = 0; i < objects.size(); i++) {
            for (int j = i + 1; j < objects.size(); j++) {
                handleCollision(objects.get(i), objects.get(j));
            }
        }

        simulationPanel.repaint();
    }

    private void applyGroundFriction(PhysicsObject obj) {
        if (obj.y + obj.radius >= simulationPanel.getHeight() - GROUND_HEIGHT) {
            double normalForce = obj.mass * GRAVITY;
            double frictionForce = GROUND_FRICTION * normalForce;
            double frictionAcceleration = frictionForce / obj.mass;

            if (obj.vx > 0) {
                obj.vx = Math.max(0, obj.vx - frictionAcceleration * TIME_STEP);
            } else if (obj.vx < 0) {
                obj.vx = Math.min(0, obj.vx + frictionAcceleration * TIME_STEP);
            }
        }
    }

    private void handleCollision(PhysicsObject obj1, PhysicsObject obj2) {
        double dx = obj2.x - obj1.x;
        double dy = obj2.y - obj1.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < obj1.radius + obj2.radius) {
            double normalX = dx / distance;
            double normalY = dy / distance;

            double relativeVelocityX = obj2.vx - obj1.vx;
            double relativeVelocityY = obj2.vy - obj1.vy;

            double normalVelocity = relativeVelocityX * normalX + relativeVelocityY * normalY;

            if (normalVelocity < 0) {
                double e = Math.min(obj1.elasticity, obj2.elasticity);
                double j = -(1 + e) * normalVelocity;
                j /= (1 / obj1.mass) + (1 / obj2.mass);

                double impulseX = j * normalX;
                double impulseY = j * normalY;

                obj1.vx -= impulseX / obj1.mass;
                obj1.vy -= impulseY / obj1.mass;
                obj2.vx += impulseX / obj2.mass;
                obj2.vy += impulseY / obj2.mass;

                double overlap = (obj1.radius + obj2.radius - distance) / 2;
                obj1.x -= overlap * normalX;
                obj1.y -= overlap * normalY;
                obj2.x += overlap * normalX;
                obj2.y += overlap * normalY;
            }
        }
    }

    // Base PhysicsObject class
    private static abstract class PhysicsObject {
        double x, y, vx, vy, radius, mass, elasticity;
        Color color;

        public PhysicsObject(double x, double y, double vx, double vy, double radius, double mass, double elasticity, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.mass = mass;
            this.elasticity = elasticity;
            this.color = color;
        }

        public abstract void draw(Graphics g);
    }

    // Circle class extending PhysicsObject
    private static class Circle extends PhysicsObject {

        public Circle(double x, double y, double vx, double vy, double radius, double mass, double elasticity, Color color) {
            super(x, y, vx, vy, radius, mass, elasticity, color);
        }

        @Override
        public void draw(Graphics g) {
            g.setColor(color);
            int diameter = (int) (radius * 2);
            g.fillOval((int) (x - radius), (int) (y - radius), diameter, diameter);
        }
    }

    // Polygon class extending PhysicsObject
    private static class PolygonObject extends PhysicsObject {
        int sides;

        public PolygonObject(double x, double y, double vx, double vy, double radius, double mass, double elasticity, int sides, Color color) {
            super(x, y, vx, vy, radius, mass, elasticity, color);
            this.sides = sides;
        }

        @Override
        public void draw(Graphics g) {
            g.setColor(color);
            int[] xPoints = new int[sides];
            int[] yPoints = new int[sides];
            for (int i = 0; i < sides; i++) {
                xPoints[i] = (int) (x + radius * Math.cos(2 * Math.PI * i / sides));
                yPoints[i] = (int) (y + radius * Math.sin(2 * Math.PI * i / sides));
            }
            g.fillPolygon(xPoints, yPoints, sides);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PhysicsSimulator simulator = new PhysicsSimulator();
            simulator.setVisible(true);
        });
    }
}
