using System.ComponentModel.DataAnnotations.Schema;

namespace FavUrls.Model.Entities
{
  public class AppUser
  {
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int AppUserId { get; set; }
    public string Email { get; set; } = null!;
    public string PasswordHash { get; set; } = null!;
    public List<Token> Tokens { get; set; } = [];
    public List<Url> Urls { get; set; } = [];
    public List<Tag> Tags { get; set; } = [];
  }
}
