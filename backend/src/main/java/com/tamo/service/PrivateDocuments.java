package com.tamo.service;
import com.tamo.model.*;
import java.nio.file.*;
import java.util.*;
public class PrivateDocuments{
 public static final Path ROOT=Paths.get("storage","uploads").toAbsolutePath().normalize();
 public static final Path LEGACY=Paths.get("storage","legacy-uploads").toAbsolutePath().normalize();
 public static void migrateLegacyDirectory() throws java.io.IOException{
  Path source=Paths.get("src/main/webapp/static/uploads/organiser").toAbsolutePath().normalize();
  if(Files.isDirectory(source)&&!Files.exists(LEGACY)){Files.createDirectories(LEGACY.getParent());Files.move(source,LEGACY);}
  // Seed examples are bundled outside the web root; private runtime copies are created only once.
  Files.createDirectories(LEGACY);
  for(String name:List.of("demo_CV.pdf","Tutorial-3-Interrupts.docx","Tutorial-3-Interrupts (1).docx")){
   Path target=LEGACY.resolve(name);
   if(!Files.exists(target))try(var input=PrivateDocuments.class.getResourceAsStream("/demo-documents/"+name)){if(input!=null)Files.copy(input,target);}
  }
 }
 public static Path path(Profile.Document doc){Path base=doc.getStorageKey()==null?LEGACY:ROOT;String name=doc.getStorageKey()==null?doc.getName():doc.getStorageKey();if(name==null||name.contains("/")||name.contains("\\")||name.contains("\r")||name.contains("\n"))throw new QualificationService.Problem(404,"Document not found.");Path p=base.resolve(name).normalize();if(!p.startsWith(base)||Files.isSymbolicLink(p))throw new QualificationService.Problem(404,"Document not found.");return p;}
 public static Profile.Document authorized(DataStore ds,String user,String docId){return authorized(ds,user,docId,null);}
 public static Profile.Document authorized(DataStore ds,String user,String docId,String applicationId){
  User u=ReviewAccess.user(ds,user);
  for(Profile p:ds.getProfiles())if(p.getDocuments()!=null)for(Profile.Document doc:p.getDocuments())if(docId.equals(doc.getId())){
   if(applicationId!=null&&ds.getApplications().stream().noneMatch(a->applicationId.equals(a.getId())&&Objects.equals(a.getApplicantId(),p.getUserId())&&a.getDocumentIds()!=null&&a.getDocumentIds().contains(docId)))continue;
   if(Objects.equals(user,p.getUserId()))return doc;
   if(ReviewAccess.can(u,"review.read"))for(Application a:ds.getApplications())if(Objects.equals(p.getUserId(),a.getApplicantId())&&a.getDocumentIds()!=null&&a.getDocumentIds().contains(docId))for(Job j:ds.getJobs())if(Objects.equals(j.getId(),a.getJobId())&&ds.getModules().stream().anyMatch(m->Objects.equals(m.getId(),j.getModuleId())&&ReviewAccess.course(u,m)))return doc;
  }throw new QualificationService.Problem(404,"Document not found or access not granted.");
 }
}
