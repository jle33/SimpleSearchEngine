package GUI;

import java.io.IOException;

import javax.swing.SwingWorker;

import SearchComponents.IndexWriter;

public class mrIndexBuilder extends SwingWorker<IndexWriter, Void> {
	private String folder;
	LoadingDialogBox dialog;
	public mrIndexBuilder(String folder) throws IOException {
		this.folder = folder;
		dialog = new LoadingDialogBox();
		this.execute();
	}
	@Override
	protected IndexWriter doInBackground() throws Exception {
		IndexWriter writer = new IndexWriter(folder);
		writer.buildIndex();
		return writer;

	}
	
	@Override
    protected void done() {
		dialog.taskDone();
	}
}
