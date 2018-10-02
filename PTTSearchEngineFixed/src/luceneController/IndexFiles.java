package luceneController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;


public class IndexFiles {
	
	public IndexFiles(){};
	
	 /** Index all text files under a directory. */
	  public static void main(String[] args) {
	    String usage = "java org.apache.lucene.demo.IndexFiles [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
	                 + "This indexes the documents in DOCS_PATH, creating a Lucene index in INDEX_PATH that can be searched with SearchFiles";
//	    String indexPath = "D:\\020518\\Downloads\\ServerFiles\\index";
//	    String docsPath = "D:\\020518\\Downloads\\ServerFiles";
	    
	    String indexPath = args[0]+"\\index";
	    String docsPath = args[0];
	    
	    boolean create = true;
	    for(int i=0;i<args.length;i++) {
	      if ("-index".equals(args[i])) {
	        indexPath = args[i+1];
	        i++;
	      } else if ("-docs".equals(args[i])) {
	        docsPath = args[i+1];
	        i++;
	      } else if ("-update".equals(args[i])) {
	        create = false;
	      }
	    }

	    if (docsPath == null) {
	      System.err.println("Usage: " + usage);
	      System.exit(1);
	    }

	    final Path docDir = Paths.get(docsPath);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    
	    Date start = new Date();
	    try {
	      System.out.println("創建索引中，路徑位置於 '" + indexPath + "'...");

	      Directory dir = FSDirectory.open(Paths.get(indexPath));   //要存放索引的路徑
//	      Analyzer analyzer = new StandardAnalyzer();				//分詞庫
//	      Analyzer analyzer = new SmartChineseAnalyzer();
//	      Analyzer analyzer = new IKAnalyzer();
	      Analyzer analyzer = new MySameAnalyzer(new SimpleSamewordContext());
	      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);	//IndexWriter的設定值

	      if (create) {
	        //創建並覆蓋掉原先的index
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	        //新增文件至已存在的index:
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }

	      // Optional: 如果資料過多，可加入此行以增加RAM值，提升效能。JVM最高提升至 (eg add -Xmx512m or -Xmx1g):
	      //
	      // iwc.setRAMBufferSizeMB(256.0);

	      IndexWriter writer = new IndexWriter(dir, iwc);
	      indexDocs(writer, docDir);

	      // NOTE: 如果想最佳化搜尋效能，可在此考慮使用forceMerge. 但這可能會非常吃資源所以通常來講，只會用在相對靜態的情況之下(在文件都已經創建完畢之後)
	      //
	      // writer.forceMerge(1);

	      writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
	    }
	  }

	  
	  static void indexDocs(final IndexWriter writer, Path path) throws IOException {
		    if (Files.isDirectory(path)) {
		      Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
		        @Override
		        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		          try {
		            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
		          } catch (IOException ignore) {
		            // don't index files that can't be read.
		          }
		          return FileVisitResult.CONTINUE;
		        }
		      });
		    } else {
		      indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
		    }
		  }
	  
	  /** 做出單一Document的index */
	  static void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
	    try (InputStream stream = Files.newInputStream(file)) {
	      // 創建一個全新的空Document
	      Document doc = new Document();
	      Metadata metadata = new Metadata();
	      // 文件的標題
	      //doc.add(new TextField("title", FilenameUtils.getBaseName(file.getFileName().toString()),Field.Store.YES));  //沒有含副檔名的格式
	      doc.add(new TextField("title", FilenameUtils.getName(file.getFileName().toString()),Field.Store.YES));    //有含副檔名的格式
	      
	      // 文件內容，有tokenized
//	      doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));  //官方預設值，標題是數字跟中文的不能找到。
//	      doc.add(new TextField("contents", new Tika().parse(stream,metadata)));   //用Tika抓取內容資訊   格式為: TextField(name, reader)，不具儲存功能。
	      
	      String content = IOUtils.toString(new Tika().parse(stream,metadata)); //挪出來，讓content可以在下面繼續使用，抓出URL。
	      doc.add(new TextField("contents", content , Field.Store.YES)); //用Tika抓取內容資訊   格式為: TextField(name, value, store)，可以儲存文件內容。
     
	      // 路徑資料-儲存
	      doc.add(new StringField("path", file.toString(), Field.Store.YES));

	      // 最後修改日期
	      doc.add(new LongField("modified", lastModified, Field.Store.YES));
	      
	      // 檔案大小
	      doc.add(new LongField("size",  file.toFile().length()  , Field.Store.YES));
      
	      // 擷取URL(頁面中出現的第一筆URL。只適用於PTT爬蟲，其他平台的話這一行則需要調整，甚至移除)
	      String link = AnalyzerUtils.extractUrls(content);
	      doc.add(new StringField("linkUrl", link , Field.Store.YES));
	      
	      if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
	        // 在加下一筆document近來
	        System.out.println("加入 " + file);
	        writer.addDocument(doc);
	      } else {
	        // 已存在的document，使用更新。
	        System.out.println("更新 " + file);
	        writer.updateDocument(new Term("path", file.toString()), doc);
	      }
	    } 
	    
	  }
	  
	  	
		
}
