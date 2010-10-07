package org.workcraft.plugins.mpsat.gui;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.workcraft.Trace;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;


@SuppressWarnings("serial")
public class SolutionsDialog extends JDialog {
	private JPanel contents;
	private JPanel solutionsPanel;
	private JPanel buttonsPanel;

	public SolutionsDialog(MpsatChainTask task, String text, List<Trace> solutions) {

		double sizes[][] = {
				{ TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED }
		};
				
		contents = new JPanel(new TableLayout(sizes));
		
		contents.add(new JLabel(text), "0 0");
		
		solutionsPanel = new JPanel();
		solutionsPanel.setLayout(new BoxLayout(solutionsPanel, BoxLayout.Y_AXIS));
		
		for (Trace t : solutions)
			solutionsPanel.add(new SolutionPanel(task, t, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SolutionsDialog.this.setVisible(false);
				}
			}));
		
		contents.add(solutionsPanel, "0 1");
		
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SolutionsDialog.this.setVisible(false);
			}
		});
		
		buttonsPanel.add(okButton);
		
		contents.add(buttonsPanel, "0 2");
		
		this.setContentPane(contents);
		
		this.setModal(true);
	}
}
