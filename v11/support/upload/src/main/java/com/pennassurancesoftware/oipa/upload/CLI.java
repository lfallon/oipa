package com.pennassurancesoftware.oipa.upload;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.jcabi.immutable.Array;
import com.pennassurancesoftware.oipa.upload.util.TempFolder;

public class CLI {
   public static abstract class Command {
      public static class _Upload extends Command {
         @Parameters(
               commandDescription = "Uploads the specified OIPA war file, libs inside war file, and classes inside war file.",
               commandNames = "upload")
         public static class Args implements Command.Args {
            @Parameter(
                  description = "Path to the OIPA war file or the folder containing the OIPA/Palette war(s) file to be uploaded.",
                  arity = 1,
                  required = false)
            public List<String> descriptors;

            @Parameter(
                  names = { "-p", "--preview" },
                  description = "Preview what will be uploaded.",
                  required = false)
            public Boolean preview = Boolean.FALSE;

            @Override
            public <T> T accept( Visitor<T> visitor ) {
               return visitor.visit( this );
            }
         }

         private final File file;
         private final Boolean preview;

         public _Upload( File file, Boolean preview ) {
            this.file = file;
            this.preview = preview;
         }

         @Override
         public _Upload execute() {
            if( preview ) {
               _preview();
            }
            else {
               _execute();
            }

            return this;
         }

         private void _execute() {
            from().uploads().forEach( this::_upload );
         }

         private void _upload( Upload.Oipa upload ) {
            System.out.println( String.format( "Uploading %s", upload ) );
            System.out.println( "----------------------------------------" );
            System.out.println( String.format( "Uploading War => %s", upload.preview().war().artifact() ) );
            upload.preview().war().upload();
            System.out.println( String.format( "Uploading Classes => %s", upload.preview().classes().artifact() ) );
            upload.preview().classes().upload();
            System.out.println( "Uploading Libs" );
            upload.preview().libs().artifacts().stream().forEach( a -> System.out.println( String.format( "  %s", a ) ) );
            upload.preview().libs().upload();
            System.out.println( "Upload Finished" );
            System.out.println( "" );
         }

         private void _preview() {
            System.out.println( "Upload" );
            System.out.println( "----------------------------------------" );
            from().uploads().forEach( System.out::println );
         }

         private Upload.Oipa.From from() {
            return Upload.Oipa.from( file );
         }

      }

      public static interface Args {
         public static interface Visitor<T> {
            public static class Default implements Visitor<Command> {
               private final transient JCommander commander;
               @SuppressWarnings("unused")
               private final transient File workingDir;

               public Default( JCommander commander, File workingDir ) {
                  this.commander = commander;
                  this.workingDir = workingDir;
               }

               private File file( List<String> descriptors ) {
                  return Optional.ofNullable( descriptors )
                        .filter( ( d ) -> d.size() >= 1 )
                        .map( ( d ) -> d.get( 0 ) )
                        .map( ( d ) -> new File( d ) )
                        .orElseGet( ( ) -> new File( "." ) );
               }

               @Override
               public Command visit( Command._Upload.Args args ) {
                  return new Command._Upload( file( args.descriptors ), args.preview );
               }

               @Override
               public Command.Help visit( Command.Help.Args args ) {
                  return new Command.Help( commander );
               }

               @Override
               public Command visit( Command.Version.Args args ) {
                  return new Command.Version( file( args.descriptors ) );
               }
            }

            T visit( _Upload.Args args );

            T visit( Help.Args args );

            T visit( Version.Args args );
         }

         public <T> T accept( Visitor<T> visitor );
      }

      public static class Help extends Command {
         @Parameters(
               commandDescription = "Display usage information.",
               commandNames = "help")
         public static class Args implements Command.Args {
            @Override
            public <T> T accept( Visitor<T> visitor ) {
               return visitor.visit( this );
            }
         }

         private final transient JCommander commander;

         public Help( JCommander commander ) {
            this.commander = commander;
         }

         @Override
         public Help execute() {
            commander.usage();
            return this;
         }
      }

      public static class Version extends Command {
         @Parameters(
               commandDescription = "Prints the version of the specified OIPA war file.",
               commandNames = "version")
         public static class Args implements Command.Args {
            @Parameter(
                  description = "Path to the OIPA war file or the folder containing the OIPA war file to be uploaded.",
                  arity = 1,
                  required = false)
            public List<String> descriptors;

            @Override
            public <T> T accept( Visitor<T> visitor ) {
               return visitor.visit( this );
            }
         }

         private final File file;

         public Version( File file ) {
            this.file = file;
         }

         @Override
         public Version execute() {
            Upload.Oipa.from( file ).uploads().forEach( System.out::println );
            // System.out.println( Upload.Oipa.from( file ).version() );
            return this;
         }
      }

      public abstract Command execute();
   }

   public static abstract class Support {
      public static abstract class Logging {
         public static class ForConsole {
            private final Optional<File> working;

            public ForConsole() {
               this( Optional.<File> empty() );
            }

            public ForConsole( Optional<File> working ) {
               this.working = working;
            }

            public File dest() {
               final File result = working();
               result.mkdirs();
               return result;
            }

            public ForConsole setup() {
               System.setProperty( PROP_LOGS_HOME, dest().getAbsolutePath() );

               return this;
            }

            public ForConsole to( File working ) {
               return new ForConsole( Optional.ofNullable( working ) );
            }

            private File working() {
               return working.orElseGet( ( ) -> new TempFolder.Default().create() );
            }
         }

         public static class Home {
            public File get() {
               return new File( System.getProperty( PROP_LOGS_HOME ) );
            }
         }

         private static final String PROP_LOGS_HOME = "LOGS_HOME";

         public static ForConsole forConsole() {
            return new ForConsole();
         }

         public static Home home() {
            return new Home();
         }
      }
   }

   public static void main( String[] args ) throws Exception {
      new CLI( Arrays.asList( args ) ).execute();
      System.exit( 0 );
   }

   private final Array<String> args;
   private final Date created = new Date();

   public CLI( Iterable<String> args ) {
      this.args = new Array<>( args );
   }

   private Command command() {
      final JCommander commander = commander();
      final String command = StringUtils.defaultIfEmpty( commander.getParsedCommand(), "help" );
      final List<Object> objects = commander().getCommands().get( command ).getObjects();
      if( objects.isEmpty() ) {
         throw new RuntimeException( String.format( "Command \"%s\" is unknown", command ) );
      }
      final Command.Args args = ( Command.Args )objects.get( 0 );
      final Command.Args.Visitor<Command> visitor = new Command.Args.Visitor.Default( commander, working() );
      return args.accept( visitor );
   }

   private JCommander commander() {
      final JCommander commander = new JCommander( this );
      commander.setProgramName( "lifeops" );
      commander.addCommand( new Command.Help.Args() );
      commander.addCommand( new Command._Upload.Args() );
      commander.addCommand( new Command.Version.Args() );
      commander.parse( args.toArray( new String[args.size()] ) );
      return commander;
   }

   public void execute() {
      logging();
      command().execute();
   }

   private void logging() {
      Support.Logging.forConsole().to( working() ).setup();
   }

   private File working() {
      final Supplier<File> home = ( ) -> new File( System.getProperty( "user.home" ) );
      final Supplier<File> program = ( ) -> new File( home.get(), ".oipa-upload-cli" );
      final Function<Date, String> fileName = ( date ) -> new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss" ).format( date );
      return new File( program.get(), fileName.apply( created ) );
   }
}
