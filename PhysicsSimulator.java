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

    private List<PhysicsObject> objects;
    private Timer timer;
    private JPanel simulationPanel;
    private Random random;

    private JTextField radiusField, massField, velocityXField, velocityYField, frictionField;
    private JSlider elasticitySlider;
    private JButton addObjectButton;

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

    private void createSimulationPanel() {
        simulationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for (PhysicsObject obj : objects) {
                    g.setColor(obj.color);
                    int diameter = (int) (obj.radius * 2);
                    g.fillOval((int) (obj.x - obj.radius), (int) (obj.y - obj.radius), diameter, diameter);
                }
            }
        };
        simulationPanel.setBackground(Color.WHITE);
        simulationPanel.setPreferredSize(new Dimension(WIDTH - 200, HEIGHT));
        add(simulationPanel, BorderLayout.CENTER);

        simulationPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                addObjectAtMouse(e.getX(), e.getY());
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
        frictionField = new JTextField("0.1", 5); 
        elasticitySlider = new JSlider(0, 100, 90);
        elasticitySlider.setMajorTickSpacing(25);
        elasticitySlider.setPaintTicks(true);
        elasticitySlider.setPaintLabels(true);

        addObjectButton = new JButton("Add Object");
        addObjectButton.addActionListener(e -> addObject());

        controlPanel.add(createLabeledComponent("Radius", radiusField));
        controlPanel.add(createLabeledComponent("Mass:", massField));
        controlPanel.add(createLabeledComponent("Velocity X:", velocityXField));
        controlPanel.add(createLabeledComponent("Velocity Y:", velocityYField));
        controlPanel.add(createLabeledComponent("Friction Coefficient:", frictionField));
        controlPanel.add(createLabeledComponent("Elasticity:", elasticitySlider));
        controlPanel.add(Box.createVerticalStrut(20));
        controlPanel.add(addObjectButton);

        add(controlPanel, BorderLayout.EAST);
    }

    private JPanel createLabeledComponent(String label, JComponent component) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(label));
        panel.add(component);
        return panel;
    }

    private void addObject() {
        try {
            double radius = Double.parseDouble(radiusField.getText());
            double mass = Double.parseDouble(massField.getText());
            double vx = Double.parseDouble(velocityXField.getText());
            double vy = Double.parseDouble(velocityYField.getText());
            double elasticity = elasticitySlider.getValue() / 100.0;
            double frictionCoefficient = Double.parseDouble(frictionField.getText());

            double x = random.nextDouble() * (simulationPanel.getWidth() - 2 * radius) + radius;
            double y = random.nextDouble() * (simulationPanel.getHeight() - 2 * radius) + radius;
            Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());

            objects.add(new PhysicsObject(x, y, vx, vy, radius, mass, elasticity, frictionCoefficient, color));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    private void addObjectAtMouse(double x, double y) {
        try {
            double radius = Double.parseDouble(radiusField.getText());
            double mass = Double.parseDouble(massField.getText());
            double vx = Double.parseDouble(velocityXField.getText());
            double vy = Double.parseDouble(velocityYField.getText());
            double elasticity = elasticitySlider.getValue() / 100.0;
            double frictionCoefficient = Double.parseDouble(frictionField.getText());

            Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());

            objects.add(new PhysicsObject(x, y, vx, vy, radius, mass, elasticity, frictionCoefficient, color));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input");
        }
    }

    private void updateSimulation() {
        for (PhysicsObject obj : objects) {
            obj.vy += GRAVITY * TIME_STEP;

            obj.x += obj.vx * TIME_STEP;
            obj.y += obj.vy * TIME_STEP;

            applyFriction(obj);

            if (obj.x - obj.radius < 0 || obj.x + obj.radius > simulationPanel.getWidth()) {
                obj.vx *= -obj.elasticity;
                obj.x = Math.max(obj.radius, Math.min(simulationPanel.getWidth() - obj.radius, obj.x));
            }
            if (obj.y - obj.radius < 0 || obj.y + obj.radius > simulationPanel.getHeight()) {
                obj.vy *= -obj.elasticity;
                obj.y = Math.max(obj.radius, Math.min(simulationPanel.getHeight() - obj.radius, obj.y));
            }
        }

        for (int i = 0; i < objects.size(); i++) {
            for (int j = i + 1; j < objects.size(); j++) {
                handleCollision(objects.get(i), objects.get(j));
            }
        }

        simulationPanel.repaint();
    }

    private void applyFriction(PhysicsObject obj) {
        if (obj.y + obj.radius >= simulationPanel.getHeight()) {
            double normalForce = obj.mass * GRAVITY;
            double frictionForce = obj.frictionCoefficient * normalForce;
            double frictionAcceleration = frictionForce / obj.mass;

            if (obj.vx > 0) {
                obj.vx = Math.max(0, obj.vx - frictionAcceleration * TIME_STEP); // Reduce vx, but not below 0
            } else if (obj.vx < 0) {
                obj.vx = Math.min(0, obj.vx + frictionAcceleration * TIME_STEP); // Increase vx towards 0
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

    private static class PhysicsObject {
        double x, y, vx, vy, radius, mass, elasticity, frictionCoefficient;
        Color color;

        public PhysicsObject(double x, double y, double vx, double vy, double radius, double mass, double elasticity, double frictionCoefficient, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.radius = radius;
            this.mass = mass;
            this.elasticity = elasticity;
            this.frictionCoefficient = frictionCoefficient;
            this.color = color;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PhysicsSimulator simulator = new PhysicsSimulator();
            simulator.setVisible(true);
        });
    }
    
}
