package CBIR_Main;

import net.semanticmetadata.lire.aggregators.Aggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.builders.LocalDocumentBuilder;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;

import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;

import com.bulenkov.darcula.DarculaLaf;

public class MainForm {

    private JButton selectDirectoryButton;
    private JTextField dirText;
    private JLabel dirLabel;
    private JTabbedPane mainTabPane;
    private JButton selectImageButton;
    private JButton createRefButton;
    private JProgressBar indexProgressBar;
    private JPanel mainPanel;
    private JScrollPane scroller;
    private JButton extractFeatureButton;
    private JLabel imagePreviewLabel;
    private JTextField inputImageField;
    private JPanel ImageHolder;
    private JButton queryImageButton;
    private JComboBox queryComboBox;
    private JPanel queryResultPanel;

    private static JFrame frame;
    private static ArrayList<String> imageList = new ArrayList<>();
    private static BufferedImage userImage;
    private String  userImagePath;
    private static IndexWriter userImageIndexWriter;
    private ImagePanel panel;

    private static String[] Manhattanstr=null;
    private static String addr;

    private final String indexPath = "index";
    private final String inputIndexPath = "index/Input";
    private final String codebookPath = "resources/";
    private final String workingdir = "C:/Users/solorankedwarrior/Documents/Computer Science/sem8/422/apam combine"; // change this string to suit your directory
    private final String testDir = "testData";

    public MainForm() {
        selectDirectoryButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(workingdir));
                fileChooser.setDialogTitle("Select Dataset Directory");
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
                {
                    createRefButton.setContentAreaFilled(true);
                    try
                    {
                        //manht: select path
                        addr=fileChooser.getSelectedFile().toString();
                        ArrayList<String> temp = FileUtils.getAllImages(new File(fileChooser.getSelectedFile().toString()), true);
                        if(temp != null)
                        {
                            //imageList.addAll(temp);
                            imageList = temp;
                            dirText.setText(fileChooser.getSelectedFile().toString());
                            dirLabel.setText("Directory Selected");
                        }
                        else
                        {
                            dirLabel.setText("Empty Directory");
                        }
                    } catch (IOException ioException)
                    {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        createRefButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (imageList.size() > 0)
                {
                    GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();

                    globalDocumentBuilder.addExtractor(CEDD.class);
                    globalDocumentBuilder.addExtractor(FCTH.class);
                    globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);
                    globalDocumentBuilder.addExtractor(ColorLayout.class);
                    // Creating an Lucene IndexWriter
                    IndexWriterConfig conf = new IndexWriterConfig(new WhitespaceAnalyzer());
                    IndexWriter iw;
                    try
                    {
                        //add manht
                        ////Manhattan.indexing(addr);
                        int count = 0;
                        iw = new IndexWriter(FSDirectory.open(Paths.get(indexPath)), conf);
                        indexProgressBar.setValue(0);
                        indexProgressBar.setString("0%");
                        // Iterating through images building the low level features
                        for (Iterator<String> it = imageList.iterator(); it.hasNext(); )
                        {
                            String imageFilePath = it.next();
                            try
                            {
                                BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                                Document document = globalDocumentBuilder.createDocument(img, imageFilePath);
                                iw.addDocument(document);
                                count++;
                                int percent = Math.round(count / imageList.size() * 100);

                                indexProgressBar.setValue(percent);
                                indexProgressBar.setString(percent + "%");

                            }
                            catch (Exception exception)
                            {
                                System.err.println("Error reading image or indexing it.");
                                exception.printStackTrace();
                            }
                        }
                        // closing the IndexWriter
                        iw.close();
                        dirLabel.setText("Reference Dataset Created");
                        System.out.println("Images successfully indexed");

                    }
                    catch (IOException ioException)
                    {
                        dirLabel.setText("Invalid Index Path");
                        System.out.println("Error openning index path: " + indexPath);
                        ioException.printStackTrace();
                    }
                }
                else
                {
                    dirLabel.setText("No Image Provided");
                    System.out.println("No Image Provided!");
                }
            }
        });
        selectImageButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                final String fileType = "Image Files";

                FileDialog dialog = new FileDialog((Frame) null, "Choose Input Image", FileDialog.LOAD);
                dialog.setMultipleMode(false);
                dialog.setModal(true);
                //dialog.setFile("*.jpg;*.jpeg;*.png");
                dialog.setVisible(true);
                userImage = null;
                try
                {
                    userImagePath = dialog.getDirectory() + dialog.getFile();
                    userImage = ImageIO.read(new File(userImagePath));
                    inputImageField.setText(userImagePath);
                    if(userImage != null)
                    {
                        ImageHolder.setLayout(new GridLayout());
                        ImageHolder.removeAll();
                        panel = new ImagePanel(userImage);
                        panel.setSize(400, 400);
                        ImageHolder.add(panel);
                        ImageHolder.repaint();
                        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();  //Global Feature Extractor
                        globalDocumentBuilder.addExtractor(CEDD.class);         //Type of Features To be Extracted
                        globalDocumentBuilder.addExtractor(FCTH.class);
                        globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);
                        globalDocumentBuilder.addExtractor(ColorLayout.class);

                        LocalDocumentBuilder localDocumentBuilder = new LocalDocumentBuilder();     //Local Feature Extractor
                        localDocumentBuilder.addExtractor(CvSurfExtractor.class,
                                Cluster.readClusters(codebookPath + "CvSURF32"));//Type of Features To be Extracted, cluster size of 32 byte??

                        Document docGlobal = globalDocumentBuilder.createDocument(userImage, userImagePath);    //Write features into a document structure
                        Document docLocal = localDocumentBuilder.createDocument(userImage, userImagePath);

                        userImageIndexWriter = LuceneUtils.createIndexWriter(inputIndexPath , true, //Create File Writer
                                LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
                        userImageIndexWriter.addDocument(docGlobal);    //Create File here
                        userImageIndexWriter.addDocument(docLocal);
                        LuceneUtils.closeWriter(userImageIndexWriter); //close file io
                    }
                }
                catch (IOException ioException) { ioException.printStackTrace(); }
            }
        });
//        extractFeatureButton.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                if(userImage != null)
//                {
//                    try
//                    {
//                        //add manht
//                        ////Manhattan.extract(userImage);
//                        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();  //Global Feature Extractor
//                        globalDocumentBuilder.addExtractor(CEDD.class);         //Type of Features To be Extracted
//                        globalDocumentBuilder.addExtractor(FCTH.class);
//                        globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);
//                        globalDocumentBuilder.addExtractor(ColorLayout.class);
//
//                        LocalDocumentBuilder localDocumentBuilder = new LocalDocumentBuilder();     //Local Feature Extractor
//                        localDocumentBuilder.addExtractor(CvSurfExtractor.class,
//                                Cluster.readClusters(codebookPath + "CvSURF32"));//Type of Features To be Extracted, cluster size of 32 byte??
//
//                        Document docGlobal = globalDocumentBuilder.createDocument(userImage, userImagePath);    //Write features into a document structure
//                        Document docLocal = localDocumentBuilder.createDocument(userImage, userImagePath);
//
//                        userImageIndexWriter = LuceneUtils.createIndexWriter(inputIndexPath , true, //Create File Writer
//                                LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
//                        userImageIndexWriter.addDocument(docGlobal);    //Create File here
//                        userImageIndexWriter.addDocument(docLocal);
//                        LuceneUtils.closeWriter(userImageIndexWriter); //close file io
//
//                        //aggregator?
//
//                        imagePreviewLabel.setText("Feature Extracted From The Image");  //UI feedback to user
//
//                    }
//                    catch (IOException ioException)
//                    {
//                        System.out.println("Error Extracting Feature.");
//                        ioException.printStackTrace();
//                    }
//                }
//                else
//                {
//                    imagePreviewLabel.setText("No Image Has Been Selected Yet.");
//                }
//            }
//        });

        queryImageButton.addActionListener(new ActionListener()
        {
            //if button click,event fired
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
                    ImageSearcher searcher = null;

                    if(queryComboBox.getSelectedItem() == "CEDD")
                    {
                        searcher = new GenericFastImageSearcher(100, CEDD.class);
                    }
                    else if(queryComboBox.getSelectedItem() == "FCTH")
                    {
                        searcher = new GenericFastImageSearcher(100, FCTH.class);
                    }
                    else if(queryComboBox.getSelectedItem() == "AutoColorCorrelogram")
                    {
                        searcher = new GenericFastImageSearcher(100, AutoColorCorrelogram.class);
                    }
                    else if(queryComboBox.getSelectedItem() == "ColorLayout") {
                        searcher = new GenericFastImageSearcher(100, ColorLayout.class);
                    }

                    // Manht: added(only comm through query function.
                    //else if(queryComboBox.getSelectedItem() == "Manhattan")
                    //{
                   //     Manhattanstr = Manhattan.retrieveall(30);
                   // }
                    queryResultPanel.setLayout( new GridLayout(5,6) );
                    if(searcher != null && userImage != null )
                    {
                        queryResultPanel.removeAll();
                        ImageSearchHits hits = searcher.search(userImage, reader);
                        for(int i = 0; i < hits.length() && hits!=null ; i++) {
                            String fileName = reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                            System.out.println(hits.score(i) + ": \t" + fileName);
                            BufferedImage image = ImageIO.read(new File(fileName));
                            JLabel label = new JLabel();
                            label.setIcon(new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(100, 80, Image.SCALE_DEFAULT)));
                            queryResultPanel.add(label);
                        }
                        searcher = null;
                    }
                    //Manht: added
//                    if (Manhattanstr!=null && userImage != null )
//                    {
//                        queryResultPanel.removeAll();
//                        for(int i = 0; i < Manhattanstr.length && Manhattanstr!=null ; i++) {
//                            BufferedImage image = ImageIO.read(new File(Manhattanstr[i]));
//                            JLabel label = new JLabel();
//                            label.setIcon(new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(100, 80, Image.SCALE_DEFAULT)));
//                            queryResultPanel.add(label);
//                        }
//                        searcher = null;
//                    }


                } catch (IOException ioException)
                {
                    ioException.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) throws IOException, UnsupportedLookAndFeelException
    {
        BasicLookAndFeel darcula = new DarculaLaf();
        UIManager.setLookAndFeel(darcula);

        frame = new JFrame("CBIR Assignment");
        frame.setContentPane(new MainForm().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        System.out.println("Working Directory = " + System.getProperty("user.dir"));
    }
}
