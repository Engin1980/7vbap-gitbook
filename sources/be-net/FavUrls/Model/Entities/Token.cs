using System.ComponentModel.DataAnnotations.Schema;

namespace FavUrls.Model.Entities
{
  public class Token
  {
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public int TokenId { get; set; }
    public string Value { get; set; } = null!;
    public AppUser AppUser { get; set; } = null!;
  }
}
